package com.financewallet.api.service.account

import com.financewallet.api.dto.request.account.CreateAccountRequest
import com.financewallet.api.dto.request.account.UpdateAccountRequest
import com.financewallet.api.dto.response.account.*
import com.financewallet.api.entity.Account
import com.financewallet.api.entity.Currency
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.AccountTypeRepository
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.repository.ExchangeRateRepository
import com.financewallet.api.service.auth.AuthService
import com.financewallet.api.service.user.UserPreferenceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val accountTypeRepository: AccountTypeRepository,
    private val currencyRepository: CurrencyRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val authService: AuthService,
    private val userPreferenceService: UserPreferenceService
) {
    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    /**
     * Create new account
     */
    @Transactional
    fun createAccount(request: CreateAccountRequest): AccountResponse {
        val currentUser = authService.getCurrentUser()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

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

        return mapToAccountResponse(savedAccount, defaultCurrency)
    }

    /**
     * Get all accounts for current user with currency conversion summary
     */
    @Transactional(readOnly = true)
    fun getAllAccounts(includeInactive: Boolean = false): List<AccountResponse> {
        val currentUser = authService.getCurrentUser()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

        val accounts = if (includeInactive) {
            accountRepository.findByUser(currentUser)
        } else {
            accountRepository.findByUserAndIsActiveTrue(currentUser)
        }

        return accounts.map { mapToAccountResponse(it, defaultCurrency) }.sortedBy { it.displayOrder }
    }

    /**
     * Get account by ID
     */
    @Transactional(readOnly = true)
    fun getAccountById(accountId: UUID): AccountResponse {
        val currentUser = authService.getCurrentUser()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

        val account = accountRepository.findByUserAndId(currentUser, accountId)
            ?: throw ResourceNotFoundException("Account not found with id: $accountId")

        return mapToAccountResponse(account, defaultCurrency)
    }

    /**
     * Update account
     */
    @Transactional
    fun updateAccount(accountId: UUID, request: UpdateAccountRequest): AccountResponse {
        val currentUser = authService.getCurrentUser()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

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

        return mapToAccountResponse(updatedAccount, defaultCurrency)
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
        // Get user's default currency
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

        val accounts = accountRepository.findByUserAndIsActiveTrue(currentUser)

        val accountResponses = accounts.map { mapToAccountResponse(it, defaultCurrency) }

        // Calculate total balance (only accounts included in total)
        val totalBalance = accountResponses
            .filter { it.isIncludedInTotal }
            .sumOf { it.balanceInDefaultCurrency }

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
            accounts = accountResponses,
            defaultCurrency = CurrencyInfo(
                id = defaultCurrency.id!!,
                name = defaultCurrency.name,
                symbol = defaultCurrency.symbol,
                code = defaultCurrency.code
            )
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
     * Convert amount to default currency using exchange rates
     */
    private fun convertToDefaultCurrency(
        amount: BigDecimal,
        fromCurrency: Currency,
        toCurrency: Currency
    ): BigDecimal {
        // If same currency, no conversion needed
        if (fromCurrency.id == toCurrency.id) {
            return amount
        }

        // Get exchange rate
        val exchangeRate = exchangeRateRepository.findLatestRate(
            fromCurrency,
            toCurrency,
            LocalDate.now()
        )

        return if (exchangeRate != null) {
            amount.multiply(exchangeRate.rate).setScale(2, RoundingMode.HALF_UP)
        } else {
            logger.warn(
                "No exchange rate found for ${fromCurrency.code} to ${toCurrency.code}. " +
                        "Using 1:1 conversion. Please add exchange rates!"
            )
            amount
        }
    }

    /**
     * Map Account entity to AccountResponse DTO
     */
    private fun mapToAccountResponse(account: Account, defaultCurrency: Currency): AccountResponse {
        val balanceInDefaultCurrency = convertToDefaultCurrency(
            amount = account.currentBalance,
            fromCurrency = account.currency,
            toCurrency = defaultCurrency
        )

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
            balanceInDefaultCurrency = balanceInDefaultCurrency,
            color = account.color,
            icon = account.icon,
            isIncludedInTotal = account.isIncludedInTotal,
            displayOrder = account.displayOrder,
            isActive = account.isActive,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}