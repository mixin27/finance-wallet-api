package com.financewallet.api.service.transaction

import com.financewallet.api.dto.request.transaction.CreateTransactionRequest
import com.financewallet.api.dto.request.transaction.TransactionFilterRequest
import com.financewallet.api.dto.request.transaction.TransferRequest
import com.financewallet.api.dto.request.transaction.UpdateTransactionRequest
import com.financewallet.api.dto.response.account.AccountTypeInfo
import com.financewallet.api.dto.response.account.CurrencyInfo
import com.financewallet.api.dto.response.transaction.*
import com.financewallet.api.entity.Transaction
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.InsufficientBalanceException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.*
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    /**
     * Create income or expense transaction
     */
    @Transactional
    fun createTransaction(request: CreateTransactionRequest): TransactionResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating ${request.type} transaction for user: ${currentUser.email}")

        // Validate account belongs to user
        val account = accountRepository.findByUserAndId(currentUser, request.accountId)
            ?: throw ResourceNotFoundException("Account not found with id: ${request.accountId}")

        // Validate amount is positive
        if (request.amount <= BigDecimal.ZERO) {
            throw BadRequestException("Amount must be greater than zero")
        }

        // Validate transaction type (not TRANSFER - use transfer endpoint for that)
        if (request.type == TransactionType.TRANSFER) {
            throw BadRequestException("Use transfer endpoint for transfer transactions")
        }

        // Validate category if provided
        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }
        }

        // Validate category type matches transaction type
        if (category != null && category.type.name != request.type.name) {
            throw BadRequestException("Category type (${category.type}) does not match transaction type (${request.type})")
        }

        // Check sufficient balance for EXPENSE
        if (request.type == TransactionType.EXPENSE) {
            if (account.currentBalance < request.amount) {
                throw InsufficientBalanceException(
                    "Insufficient balance in account '${account.name}'. " +
                            "Available: ${account.currency.symbol}${account.currentBalance}, " +
                            "Required: ${account.currency.symbol}${request.amount}"
                )
            }
        }

        // Create transaction
        val transaction = Transaction(
            user = currentUser,
            account = account,
            category = category,
            type = request.type,
            amount = request.amount,
            currency = account.currency,
            transactionDate = request.transactionDate,
            description = request.description,
            note = request.note,
            payee = request.payee,
            location = request.location,
            latitude = request.latitude,
            longitude = request.longitude
        )

        // Process tags
        if (!request.tags.isNullOrEmpty()) {
            request.tags.forEach { tagName ->
                val tag = tagRepository.findByUserAndName(currentUser, tagName)
                    ?: tagRepository.save(com.financewallet.api.entity.Tag(
                        user = currentUser,
                        name = tagName
                    ))
                transaction.tags.add(tag)
            }
        }

        // Update account balance
        when (request.type) {
            TransactionType.INCOME -> account.currentBalance += request.amount
            TransactionType.EXPENSE -> account.currentBalance -= request.amount
            else -> {}
        }
        account.updatedAt = LocalDateTime.now()
        accountRepository.save(account)

        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Transaction created successfully: ${savedTransaction.id}")

        return mapToTransactionResponse(savedTransaction)
    }

    /**
     * Transfer between accounts
     */
    @Transactional
    fun transfer(request: TransferRequest): TransactionResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating transfer for user: ${currentUser.email}")

        // Validate accounts
        val fromAccount = accountRepository.findByUserAndId(currentUser, request.fromAccountId)
            ?: throw ResourceNotFoundException("From account not found with id: ${request.fromAccountId}")

        val toAccount = accountRepository.findByUserAndId(currentUser, request.toAccountId)
            ?: throw ResourceNotFoundException("To account not found with id: ${request.toAccountId}")

        // Cannot transfer to same account
        if (fromAccount.id == toAccount.id) {
            throw BadRequestException("Cannot transfer to the same account")
        }

        // Validate amount
        if (request.amount <= BigDecimal.ZERO) {
            throw BadRequestException("Transfer amount must be greater than zero")
        }

        // Check sufficient balance
        if (fromAccount.currentBalance < request.amount) {
            throw InsufficientBalanceException(
                "Insufficient balance in account '${fromAccount.name}'. " +
                        "Available: ${fromAccount.currency.symbol}${fromAccount.currentBalance}, " +
                        "Required: ${fromAccount.currency.symbol}${request.amount}"
            )
        }

        // Handle currency conversion
        val sameCurrency = fromAccount.currency.id == toAccount.currency.id
        val exchangeRate = if (sameCurrency) {
            BigDecimal.ONE
        } else {
            request.exchangeRate ?: throw BadRequestException(
                "Exchange rate is required for transfers between different currencies"
            )
        }

        val convertedAmount = request.amount * exchangeRate

        // Create transfer transaction
        val transaction = Transaction(
            user = currentUser,
            account = fromAccount,
            toAccount = toAccount,
            type = TransactionType.TRANSFER,
            amount = request.amount,
            currency = fromAccount.currency,
            exchangeRate = exchangeRate,
            convertedAmount = convertedAmount,
            transactionDate = request.transactionDate,
            description = request.description,
            note = request.note
        )

        // Update balances
        fromAccount.currentBalance -= request.amount
        fromAccount.updatedAt = LocalDateTime.now()

        toAccount.currentBalance += convertedAmount
        toAccount.updatedAt = LocalDateTime.now()

        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Transfer completed successfully: ${savedTransaction.id}")

        return mapToTransactionResponse(savedTransaction)
    }

    /**
     * Get all transactions with filters
     */
    @Transactional(readOnly = true)
    fun getTransactions(filter: TransactionFilterRequest): Page<TransactionResponse> {
        val currentUser = authService.getCurrentUser()

        val sort = Sort.by(
            if (filter.sortDirection == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
            filter.sortBy
        )
        val pageable = PageRequest.of(filter.page, filter.size, sort)

        val transactionsPage = when {
            filter.accountId != null -> {
                val account = accountRepository.findByUserAndId(currentUser, filter.accountId)
                    ?: throw ResourceNotFoundException("Account not found")
                transactionRepository.findByUserAndAccount(currentUser, account, pageable)
            }
            filter.categoryId != null -> {
                val category = categoryRepository.findById(filter.categoryId)
                    .orElseThrow { ResourceNotFoundException("Category not found") }
                transactionRepository.findByUserAndCategory(currentUser, category, pageable)
            }
            filter.type != null -> {
                transactionRepository.findByUserAndType(currentUser, filter.type, pageable)
            }
            filter.startDate != null && filter.endDate != null -> {
                transactionRepository.findByUserAndDateRange(
                    currentUser, filter.startDate, filter.endDate, pageable
                )
            }
            else -> {
                transactionRepository.findByUser(currentUser, pageable)
            }
        }

        // Map to response DTOs - tags are already fetched via LEFT JOIN FETCH
        return transactionsPage.map { mapToTransactionResponse(it) }
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    fun getTransactionById(transactionId: UUID): TransactionResponse {
        val currentUser = authService.getCurrentUser()

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $transactionId") }

        // Verify transaction belongs to current user
        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found with id: $transactionId")
        }

        return mapToTransactionResponse(transaction)
    }

    /**
     * Update transaction
     */
    @Transactional
    fun updateTransaction(transactionId: UUID, request: UpdateTransactionRequest): TransactionResponse {
        val currentUser = authService.getCurrentUser()

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $transactionId") }

        // Verify ownership
        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found with id: $transactionId")
        }

        // Cannot update transfer transactions (delete and recreate instead)
        if (transaction.type == TransactionType.TRANSFER) {
            throw BadRequestException("Cannot update transfer transactions. Delete and create a new one instead.")
        }

        logger.info("Updating transaction: ${transaction.id}")

        val oldAmount = transaction.amount
        val account = transaction.account

        // Update fields if provided
        request.amount?.let {
            if (it <= BigDecimal.ZERO) {
                throw BadRequestException("Amount must be greater than zero")
            }

            // Revert old amount effect
            when (transaction.type) {
                TransactionType.INCOME -> account.currentBalance -= oldAmount
                TransactionType.EXPENSE -> account.currentBalance += oldAmount
                else -> {}
            }

            // Apply new amount
            transaction.amount = it
            when (transaction.type) {
                TransactionType.INCOME -> account.currentBalance += it
                TransactionType.EXPENSE -> {
                    if (account.currentBalance < it) {
                        throw InsufficientBalanceException("Insufficient balance for updated amount")
                    }
                    account.currentBalance -= it
                }
                else -> {}
            }
        }

        request.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }
            transaction.category = category
        }

        request.transactionDate?.let { transaction.transactionDate = it }
        request.description?.let { transaction.description = it }
        request.note?.let { transaction.note = it }
        request.payee?.let { transaction.payee = it }
        request.location?.let { transaction.location = it }
        request.latitude?.let { transaction.latitude = it }
        request.longitude?.let { transaction.longitude = it }

        // Update tags if provided
        request.tags?.let { tagNames ->
            transaction.tags.clear()
            tagNames.forEach { tagName ->
                val tag = tagRepository.findByUserAndName(currentUser, tagName)
                    ?: tagRepository.save(com.financewallet.api.entity.Tag(
                        user = currentUser,
                        name = tagName
                    ))
                transaction.tags.add(tag)
            }
        }

        transaction.updatedAt = LocalDateTime.now()
        account.updatedAt = LocalDateTime.now()

        accountRepository.save(account)
        val updatedTransaction = transactionRepository.save(transaction)

        logger.info("Transaction updated successfully")
        return mapToTransactionResponse(updatedTransaction)
    }

    /**
     * Delete transaction
     */
    @Transactional
    fun deleteTransaction(transactionId: UUID) {
        val currentUser = authService.getCurrentUser()

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $transactionId") }

        // Verify ownership
        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found with id: $transactionId")
        }

        logger.info("Deleting transaction: ${transaction.id}")

        // Revert account balance changes
        val account = transaction.account
        when (transaction.type) {
            TransactionType.INCOME -> account.currentBalance -= transaction.amount
            TransactionType.EXPENSE -> account.currentBalance += transaction.amount
            TransactionType.TRANSFER -> {
                // Revert both accounts
                account.currentBalance += transaction.amount
                transaction.toAccount?.let { toAccount ->
                    toAccount.currentBalance -= (transaction.convertedAmount ?: transaction.amount)
                    toAccount.updatedAt = LocalDateTime.now()
                    accountRepository.save(toAccount)
                }
            }
        }
        account.updatedAt = LocalDateTime.now()
        accountRepository.save(account)

        transactionRepository.delete(transaction)
        logger.info("Transaction deleted successfully")
    }

    /**
     * Map Transaction entity to TransactionResponse DTO
     * TODO: fix java.util.ConcurrentModificationException: null
     */
    private fun mapToTransactionResponse(transaction: Transaction): TransactionResponse {
        return TransactionResponse(
            id = transaction.id!!,
            account = TransactionAccountInfo(
                id = transaction.account.id!!,
                name = transaction.account.name,
                accountType = AccountTypeInfo(
                    id = transaction.account.accountType.id!!,
                    name = transaction.account.accountType.name,
                    icon = transaction.account.accountType.icon
                ),
                currency = CurrencyInfo(
                    id = transaction.account.currency.id!!,
                    code = transaction.account.currency.code,
                    symbol = transaction.account.currency.symbol,
                    name = transaction.account.currency.name
                )
            ),
            toAccount = transaction.toAccount?.let {
                TransactionAccountInfo(
                    id = it.id!!,
                    name = it.name,
                    accountType = AccountTypeInfo(
                        id = it.accountType.id!!,
                        name = it.accountType.name,
                        icon = it.accountType.icon
                    ),
                    currency = CurrencyInfo(
                        id = it.currency.id!!,
                        code = it.currency.code,
                        symbol = it.currency.symbol,
                        name = it.currency.name
                    )
                )
            },
            category = transaction.category?.let {
                CategoryInfo(
                    id = it.id!!,
                    name = it.name,
                    type = it.type.name,
                    color = it.color,
                    icon = it.icon
                )
            },
            type = transaction.type.name,
            amount = transaction.amount,
            currency = CurrencyInfo(
                id = transaction.currency.id!!,
                code = transaction.currency.code,
                symbol = transaction.currency.symbol,
                name = transaction.currency.name
            ),
            exchangeRate = transaction.exchangeRate,
            convertedAmount = transaction.convertedAmount,
            transactionDate = transaction.transactionDate,
            description = transaction.description ?: "",
            note = transaction.note,
            payee = transaction.payee,
            location = transaction.location,
            latitude = transaction.latitude,
            longitude = transaction.longitude,
            status = transaction.status.name,
            tags = transaction.tags.map { tag ->
                TagInfo(
                    id = tag.id!!,
                    name = tag.name,
                    color = tag.color
                )
            },
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )
    }
}