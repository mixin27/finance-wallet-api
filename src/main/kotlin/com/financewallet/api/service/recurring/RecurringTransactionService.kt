package com.financewallet.api.service.recurring

import com.financewallet.api.dto.request.recurring.CreateRecurringTransactionRequest
import com.financewallet.api.dto.request.recurring.UpdateRecurringTransactionRequest
import com.financewallet.api.dto.response.recurring.RecurringTransactionResponse
import com.financewallet.api.entity.*
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.CategoryRepository
import com.financewallet.api.repository.RecurringTransactionRepository
import com.financewallet.api.repository.TransactionRepository
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class RecurringTransactionService(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(RecurringTransactionService::class.java)

    /**
     * Create recurring transaction
     */
    @Transactional
    fun createRecurringTransaction(request: CreateRecurringTransactionRequest): RecurringTransactionResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating recurring transaction for user: ${currentUser.email}")

        // Validate account
        val account = accountRepository.findByUserAndId(currentUser, request.accountId)
            ?: throw ResourceNotFoundException("Account not found")

        // Validate to-account for transfers
        val toAccount = if (request.type == TransactionType.TRANSFER) {
            request.toAccountId?.let { toAccountId ->
                accountRepository.findByUserAndId(currentUser, toAccountId)
                    ?: throw ResourceNotFoundException("Destination account not found")
            } ?: throw BadRequestException("Destination account is required for transfers")
        } else null

        // Validate category
        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }
        }

        // Validate dates
        if (request.endDate != null && request.endDate < request.startDate) {
            throw BadRequestException("End date must be after start date")
        }

        // Validate amount
        if (request.amount <= java.math.BigDecimal.ZERO) {
            throw BadRequestException("Amount must be greater than zero")
        }

        val recurringTransaction = RecurringTransaction(
            user = currentUser,
            account = account,
            toAccount = toAccount,
            category = category,
            type = request.type,
            amount = request.amount,
            currency = account.currency,
            description = request.description,
            frequency = request.frequency,
            intervalValue = request.intervalValue,
            startDate = request.startDate,
            endDate = request.endDate,
            nextOccurrenceDate = request.startDate
        )

        val saved = recurringTransactionRepository.save(recurringTransaction)
        logger.info("Recurring transaction created: ${saved.id}")

        return mapToResponse(saved)
    }

    /**
     * Get all recurring transactions
     */
    @Transactional(readOnly = true)
    fun getAllRecurringTransactions(activeOnly: Boolean = false): List<RecurringTransactionResponse> {
        val currentUser = authService.getCurrentUser()

        val transactions = if (activeOnly) {
            recurringTransactionRepository.findByUserAndIsActiveTrue(currentUser)
        } else {
            recurringTransactionRepository.findByUser(currentUser)
        }

        return transactions.map { mapToResponse(it) }
    }

    /**
     * Get recurring transaction by ID
     */
    @Transactional(readOnly = true)
    fun getRecurringTransactionById(id: UUID): RecurringTransactionResponse {
        val currentUser = authService.getCurrentUser()

        val recurring = recurringTransactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recurring transaction not found") }

        if (recurring.user.id != currentUser.id) {
            throw ResourceNotFoundException("Recurring transaction not found")
        }

        return mapToResponse(recurring)
    }

    /**
     * Update recurring transaction
     */
    @Transactional
    fun updateRecurringTransaction(id: UUID, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse {
        val currentUser = authService.getCurrentUser()

        val recurring = recurringTransactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recurring transaction not found") }

        if (recurring.user.id != currentUser.id) {
            throw ResourceNotFoundException("Recurring transaction not found")
        }

        logger.info("Updating recurring transaction: $id")

        request.amount?.let { recurring.amount = it }
        request.description?.let { recurring.description = it }
        request.isActive?.let { recurring.isActive = it }
        request.endDate?.let { recurring.endDate = it }

        recurring.updatedAt = LocalDateTime.now()

        val updated = recurringTransactionRepository.save(recurring)
        logger.info("Recurring transaction updated")

        return mapToResponse(updated)
    }

    /**
     * Delete recurring transaction
     */
    @Transactional
    fun deleteRecurringTransaction(id: UUID) {
        val currentUser = authService.getCurrentUser()

        val recurring = recurringTransactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recurring transaction not found") }

        if (recurring.user.id != currentUser.id) {
            throw ResourceNotFoundException("Recurring transaction not found")
        }

        logger.info("Deleting recurring transaction: $id")
        recurringTransactionRepository.delete(recurring)
        logger.info("Recurring transaction deleted")
    }

    /**
     * Process due recurring transactions (scheduled job)
     * Runs daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    fun processDueRecurringTransactions() {
        logger.info("Processing due recurring transactions...")

        val today = LocalDate.now()
        val dueTransactions = recurringTransactionRepository.findDueRecurringTransactions(today)

        logger.info("Found ${dueTransactions.size} due recurring transactions")

        for (recurring in dueTransactions) {
            try {
                generateTransaction(recurring)
                updateNextOccurrence(recurring)
            } catch (e: Exception) {
                logger.error("Failed to process recurring transaction ${recurring.id}", e)
            }
        }

        logger.info("Finished processing recurring transactions")
    }

    /**
     * Generate transaction from recurring template
     */
    private fun generateTransaction(recurring: RecurringTransaction) {
        val transaction = Transaction(
            user = recurring.user,
            account = recurring.account,
            toAccount = recurring.toAccount,
            category = recurring.category,
            type = recurring.type,
            amount = recurring.amount,
            currency = recurring.currency,
            transactionDate = LocalDateTime.now(),
            description = recurring.description,
            isRecurring = true,
            recurringTransactionId = recurring.id,
            status = TransactionStatus.COMPLETED
        )

        // Update account balances
        when (recurring.type) {
            TransactionType.INCOME -> {
                recurring.account.currentBalance += recurring.amount
            }
            TransactionType.EXPENSE -> {
                recurring.account.currentBalance -= recurring.amount
            }
            TransactionType.TRANSFER -> {
                recurring.account.currentBalance -= recurring.amount
                recurring.toAccount?.let {
                    it.currentBalance += recurring.amount
                    accountRepository.save(it)
                }
            }
        }

        recurring.account.updatedAt = LocalDateTime.now()
        accountRepository.save(recurring.account)

        transactionRepository.save(transaction)

        recurring.lastGeneratedDate = LocalDate.now()
        recurringTransactionRepository.save(recurring)

        logger.info("Generated transaction from recurring template ${recurring.id}")
    }

    /**
     * Calculate next occurrence date
     */
    private fun updateNextOccurrence(recurring: RecurringTransaction) {
        val nextDate = when (recurring.frequency) {
            RecurringFrequency.DAILY -> recurring.nextOccurrenceDate.plusDays(recurring.intervalValue.toLong())
            RecurringFrequency.WEEKLY -> recurring.nextOccurrenceDate.plusWeeks(recurring.intervalValue.toLong())
            RecurringFrequency.MONTHLY -> recurring.nextOccurrenceDate.plusMonths(recurring.intervalValue.toLong())
            RecurringFrequency.YEARLY -> recurring.nextOccurrenceDate.plusYears(recurring.intervalValue.toLong())
        }

        // Check if recurring should be deactivated
        if (recurring.endDate != null && nextDate > recurring.endDate) {
            recurring.isActive = false
            logger.info("Recurring transaction ${recurring.id} has ended")
        } else {
            recurring.nextOccurrenceDate = nextDate
        }

        recurring.updatedAt = LocalDateTime.now()
        recurringTransactionRepository.save(recurring)
    }

    /**
     * Map to response DTO
     */
    private fun mapToResponse(recurring: RecurringTransaction): RecurringTransactionResponse {
        return RecurringTransactionResponse(
            id = recurring.id!!,
            accountId = recurring.account.id!!,
            accountName = recurring.account.name,
            toAccountId = recurring.toAccount?.id,
            toAccountName = recurring.toAccount?.name,
            categoryId = recurring.category?.id,
            categoryName = recurring.category?.name,
            type = recurring.type.name,
            amount = recurring.amount,
            currencyCode = recurring.currency.code,
            currencySymbol = recurring.currency.symbol,
            description = recurring.description,
            frequency = recurring.frequency.name,
            intervalValue = recurring.intervalValue,
            startDate = recurring.startDate,
            endDate = recurring.endDate,
            nextOccurrenceDate = recurring.nextOccurrenceDate,
            lastGeneratedDate = recurring.lastGeneratedDate,
            isActive = recurring.isActive,
            createdAt = recurring.createdAt,
            updatedAt = recurring.updatedAt
        )
    }
}