package com.financewallet.api.service.account

import com.financewallet.api.dto.request.account.CreateAccountRequest
import com.financewallet.api.dto.request.account.UpdateAccountRequest
import com.financewallet.api.dto.response.account.*
import com.financewallet.api.entity.Account
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.AccountTypeRepository
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val accountTypeRepository: AccountTypeRepository,
    private val currencyRepository: CurrencyRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    /**
     * Create new account
     */
    @Transactional
    fun createAccount(request: CreateAccountRequest): AccountResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating new account for user: ${currentUser.email}")

        // Validate account type
        val accountType = accountTypeRepository.findById(request.accountTypeId)
            .orElseThrow { ResourceNotFoundException("Account type not found with id: ${request.accountTypeId}") }

        // Validate currency
        val currency = currencyRepository.findById(request.currencyId)
            .orElseThrow { ResourceNotFoundException("Currency not found with id: ${request.currencyId}") }

        // Validate color format if provided
        if (request.color != null && !request.color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
            throw BadRequestException("Invalid color format. Use hex format: #RRGGBB")
        }

        // Create account
        val account = Account(
            user = currentUser,
            accountType = accountType,
            currency = currency,
            name = request.name,
            description = request.description,
            initialBalance = request.initialBalance,
            currentBalance = request.initialBalance, // Initial balance = current balance on creation
            color = request.color,
            icon = request.icon,
            isIncludedInTotal = request.isIncludedInTotal
        )

        val savedAccount = accountRepository.save(account)
        logger.info("Account created successfully: ${savedAccount.name} (${savedAccount.id})")

        return mapToAccountResponse(savedAccount)
    }

    /**
     * Get all accounts for current user
     */
    @Transactional(readOnly = true)
    fun getAllAccounts(includeInactive: Boolean = false): List<AccountResponse> {
        val currentUser = authService.getCurrentUser()

        val accounts = if (includeInactive) {
            accountRepository.findByUser(currentUser)
        } else {
            accountRepository.findByUserAndIsActiveTrue(currentUser)
        }

        return accounts.map { mapToAccountResponse(it) }
    }

    /**
     * Get account by ID
     */
    @Transactional(readOnly = true)
    fun getAccountById(accountId: UUID): AccountResponse {
        val currentUser = authService.getCurrentUser()

        val account = accountRepository.findByUserAndId(currentUser, accountId)
            ?: throw ResourceNotFoundException("Account not found with id: $accountId")

        return mapToAccountResponse(account)
    }

    /**
     * Update account
     */
    @Transactional
    fun updateAccount(accountId: UUID, request: UpdateAccountRequest): AccountResponse {
        val currentUser = authService.getCurrentUser()

        val account = accountRepository.findByUserAndId(currentUser, accountId)
            ?: throw ResourceNotFoundException("Account not found with id: $accountId")

        logger.info("Updating account: ${account.name} (${account.id})")

        // Update fields if provided
        request.name?.let { account.name = it }
        request.description?.let { account.description = it }
        request.color?.let {
            if (!it.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
                throw BadRequestException("Invalid color format. Use hex format: #RRGGBB")
            }
            account.color = it
        }
        request.icon?.let { account.icon = it }
        request.isIncludedInTotal?.let { account.isIncludedInTotal = it }
        request.isActive?.let { account.isActive = it }

        // Update account type if provided
        request.accountTypeId?.let { typeId ->
            val accountType = accountTypeRepository.findById(typeId)
                .orElseThrow { ResourceNotFoundException("Account type not found with id: $typeId") }
            account.accountType = accountType
        }

        // Update currency if provided
        request.currencyId?.let { currencyId ->
            val currency = currencyRepository.findById(currencyId)
                .orElseThrow { ResourceNotFoundException("Currency not found with id: $currencyId") }
            account.currency = currency
        }

        account.updatedAt = LocalDateTime.now()
        val updatedAccount = accountRepository.save(account)

        logger.info("Account updated successfully: ${updatedAccount.name}")

        return mapToAccountResponse(updatedAccount)
    }

    /**
     * Delete account (soft delete)
     */
    @Transactional
    fun deleteAccount(accountId: UUID) {
        val currentUser = authService.getCurrentUser()

        val account = accountRepository.findByUserAndId(currentUser, accountId)
            ?: throw ResourceNotFoundException("Account not found with id: $accountId")

        // Check if account has balance
        if (account.currentBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw BadRequestException("Cannot delete account with non-zero balance. Current balance: ${account.currency.symbol}${account.currentBalance}")
        }

        // Soft delete
        account.isActive = false
        account.updatedAt = LocalDateTime.now()
        accountRepository.save(account)

        logger.info("Account deleted (soft): ${account.name} (${account.id})")
    }

    /**
     * Get account summary with statistics
     */
    @Transactional(readOnly = true)
    fun getAccountSummary(): AccountSummaryResponse {
        val currentUser = authService.getCurrentUser()

        val accounts = accountRepository.findByUserAndIsActiveTrue(currentUser)
        val accountResponses = accounts.map { mapToAccountResponse(it) }

        // Calculate total balance (only accounts included in total)
        val totalBalance = accounts
            .filter { it.isIncludedInTotal }
            .sumOf { it.currentBalance }

        // Group by currency
        val balanceByCurrency = accounts
            .filter { it.isIncludedInTotal }
            .groupBy { it.currency }
            .map { (currency, accts) ->
                CurrencyBalance(
                    currencyCode = currency.code,
                    currencySymbol = currency.symbol,
                    balance = accts.sumOf { it.currentBalance },
                    accountCount = accts.size
                )
            }
            .sortedByDescending { it.balance }

        return AccountSummaryResponse(
            totalAccounts = accounts.size,
            activeAccounts = accounts.count { it.isActive },
            totalBalance = totalBalance,
            balanceByCurrency = balanceByCurrency,
            accounts = accountResponses
        )
    }

    /**
     * Get account types (for dropdown in UI)
     */
    @Transactional(readOnly = true)
    fun getAllAccountTypes(): List<AccountTypeInfo> {
        return accountTypeRepository.findAll().map {
            AccountTypeInfo(
                id = it.id!!,
                name = it.name,
                icon = it.icon
            )
        }
    }

    /**
     * Map Account entity to AccountResponse DTO
     */
    private fun mapToAccountResponse(account: Account): AccountResponse {
        return AccountResponse(
            id = account.id!!,
            name = account.name,
            accountType = AccountTypeInfo(
                id = account.accountType.id!!,
                name = account.accountType.name,
                icon = account.accountType.icon
            ),
            currency = CurrencyInfo(
                id = account.currency.id!!,
                code = account.currency.code,
                symbol = account.currency.symbol,
                name = account.currency.name
            ),
            description = account.description,
            initialBalance = account.initialBalance,
            currentBalance = account.currentBalance,
            color = account.color,
            icon = account.icon,
            isIncludedInTotal = account.isIncludedInTotal,
            isActive = account.isActive,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}