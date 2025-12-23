package com.financewallet.api.config

import com.financewallet.api.entity.AccountType
import com.financewallet.api.entity.Currency
import com.financewallet.api.entity.ExchangeRate
import com.financewallet.api.repository.AccountTypeRepository
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.repository.ExchangeRateRepository
import com.financewallet.api.service.category.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.TypeReference.listOf
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Initialize default data when application starts
 */
@Component
class DataInitializer(
    private val categoryService: CategoryService,
    private val accountTypeRepository: AccountTypeRepository,
    private val currencyRepository: CurrencyRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting data initialization...")

        try {
            // Initialize currencies
            initializeCurrencies()
            // Initialize account types
            initializeAccountTypes()
            // Initialize exchange rates
            initializeExchangeRates()

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
            AccountType(name = "Checking", description = "Standard checking account", icon = "💳"),
            AccountType(name = "Savings", description = "Savings account", icon = "🏦"),
            AccountType(name = "Credit Card", description = "Credit card account", icon = "💳"),
            AccountType(name = "Cash", description = "Physical cash", icon = "💵"),
            AccountType(name = "Investment", description = "Investment account", icon = "📈"),
            AccountType(name = "Loan", description = "Loan account", icon = "🏦"),
            AccountType(name = "Mortgage", description = "Home mortgage", icon = "🏠"),
            AccountType(name = "Digital Wallet", description = "PayPal, Venmo, etc.", icon = "📱")
        )

        for (act in accountTypes) {
            val exit = accountTypeRepository.findByName(act.name)
            if (exit == null) {
                accountTypeRepository.save(act)
            }
        }
        logger.info("Account types initialized successfully")
    }

    private fun initializeCurrencies() {
        logger.info("Initializing default currencies...")

        val currencies = listOf(
            Currency(code = "USD", name = "US Dollar", symbol = "$", decimalPlaces = 2),
            Currency(code = "EUR", name = "Euro", symbol = "€", decimalPlaces = 2),
            Currency(code = "MMK", name = "Myanmar Kyat", symbol = "MMK", decimalPlaces = 2)
        )

        for (currency in currencies) {
            val exist = currencyRepository.findByCode(currency.code)
            if (exist == null) {
                currencyRepository.save(currency)
            }
        }
        logger.info("Currencies initialized successfully")
    }

    /**
     * Initialize exchange rates for common currency pairs
     * Rates are approximate as of December 2024
     */
    private fun initializeExchangeRates() {
        val existingCount = exchangeRateRepository.count()
        if (existingCount > 0) {
            logger.info("Exchange rates already initialized ($existingCount rates found)")
            return
        }

        logger.info("Initializing exchange rates...")

        val today = LocalDate.now()
        val rates = mutableListOf<ExchangeRate>()

        // Helper function to add bidirectional rates
        fun addRate(from: String, to: String, rate: BigDecimal) {
            val fromCurrency = currencyRepository.findByCode(from)
            val toCurrency = currencyRepository.findByCode(to)

            if (fromCurrency != null && toCurrency != null) {
                rates.add(ExchangeRate(fromCurrency = fromCurrency, toCurrency = toCurrency, rate = rate, effectiveDate = today))
                // Add reverse rate
                val reverseRate = BigDecimal.ONE.divide(rate, 8, java.math.RoundingMode.HALF_UP)
                rates.add(ExchangeRate(fromCurrency = toCurrency, toCurrency = fromCurrency, rate = reverseRate, effectiveDate = today))
            }
        }

        // USD as base currency
        addRate("USD", "EUR", BigDecimal("0.92"))
        addRate("USD", "MMK", BigDecimal("3900.00"))

        // EUR to other currencies
        addRate("EUR", "MMK", BigDecimal("4575.00"))

        exchangeRateRepository.saveAll(rates)
        logger.info("${rates.size} exchange rates initialized successfully")
        logger.info("Exchange rates updated as of: $today")
    }
}