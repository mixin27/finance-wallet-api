package com.financewallet.api.config

import com.financewallet.api.entity.AccountType
import com.financewallet.api.entity.Currency
import com.financewallet.api.repository.AccountTypeRepository
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.service.category.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.TypeReference.listOf
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Initialize default data when application starts
 */
@Component
class DataInitializer(
    private val categoryService: CategoryService,
    private val accountTypeRepository: AccountTypeRepository,
    private val currencyRepository: CurrencyRepository
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting data initialization...")

        try {
            // Initialize account types
            initializeAccountTypes()

            // Initialize currencies
            initializeCurrencies()

            // Initialize system categories
            categoryService.initializeSystemCategories()

            logger.info("Data initialization completed successfully")
        } catch (e: Exception) {
            logger.error("Error during data initialization", e)
        }
    }

    private fun initializeAccountTypes() {
        if (accountTypeRepository.count() > 0) {
            logger.info("Account types already initialized")
            return
        }

        logger.info("Initializing default account types...")

        val accountTypes = listOf(
            AccountType(name = "Checking", description = "Checking account", icon = "💳"),
            AccountType(name = "Savings", description = "Savings account", icon = "🏦"),
            AccountType(name = "Credit Card", description = "Credit card account", icon = "💳"),
            AccountType(name = "Cash", description = "Cash account", icon = "💵"),
            AccountType(name = "Investment", description = "Investment account", icon = "📈"),
            AccountType(name = "Loan", description = "Loan account", icon = "🏦")
        )

        accountTypeRepository.saveAll(accountTypes)
        logger.info("Account types initialized successfully")
    }

    private fun initializeCurrencies() {
        if (currencyRepository.count() > 0) {
            logger.info("Currencies already initialized")
            return
        }

        logger.info("Initializing default currencies...")

        val currencies = listOf(
            Currency(code = "USD", name = "US Dollar", symbol = "$", decimalPlaces = 2),
            Currency(code = "EUR", name = "Euro", symbol = "€", decimalPlaces = 2),
            Currency(code = "GBP", name = "British Pound", symbol = "£", decimalPlaces = 2),
            Currency(code = "JPY", name = "Japanese Yen", symbol = "¥", decimalPlaces = 0),
            Currency(code = "CNY", name = "Chinese Yuan", symbol = "¥", decimalPlaces = 2),
            Currency(code = "INR", name = "Indian Rupee", symbol = "₹", decimalPlaces = 2),
            Currency(code = "AUD", name = "Australian Dollar", symbol = "A$", decimalPlaces = 2),
            Currency(code = "CAD", name = "Canadian Dollar", symbol = "C$", decimalPlaces = 2),
            Currency(code = "CHF", name = "Swiss Franc", symbol = "Fr", decimalPlaces = 2),
            Currency(code = "SEK", name = "Swedish Krona", symbol = "kr", decimalPlaces = 2)
        )

        currencyRepository.saveAll(currencies)
        logger.info("Currencies initialized successfully")
    }
}