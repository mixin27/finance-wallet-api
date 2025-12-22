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

        accountTypeRepository.saveAll(accountTypes)
        logger.info("Account types initialized successfully")
    }

    private fun initializeCurrencies() {
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
            Currency(code = "SEK", name = "Swedish Krona", symbol = "kr", decimalPlaces = 2),
            Currency(code = "NZD", name = "New Zealand Dollar", symbol = "NZ$", decimalPlaces = 2),
            Currency(code = "SGD", name = "Singapore Dollar", symbol = "S$", decimalPlaces = 2),
            Currency(code = "HKD", name = "Hong Kong Dollar", symbol = "HK$", decimalPlaces = 2),
            Currency(code = "KRW", name = "South Korean Won", symbol = "₩", decimalPlaces = 0),
            Currency(code = "MXN", name = "Mexican Peso", symbol = "$", decimalPlaces = 2),
            Currency(code = "BRL", name = "Brazilian Real", symbol = "R$", decimalPlaces = 2),
            Currency(code = "ZAR", name = "South African Rand", symbol = "R", decimalPlaces = 2),
            Currency(code = "RUB", name = "Russian Ruble", symbol = "₽", decimalPlaces = 2),
            Currency(code = "TRY", name = "Turkish Lira", symbol = "₺", decimalPlaces = 2),
            Currency(code = "AED", name = "UAE Dirham", symbol = "د.إ", decimalPlaces = 2),
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
        addRate("USD", "GBP", BigDecimal("0.79"))
        addRate("USD", "JPY", BigDecimal("149.50"))
        addRate("USD", "CNY", BigDecimal("7.24"))
        addRate("USD", "INR", BigDecimal("83.12"))
        addRate("USD", "AUD", BigDecimal("1.52"))
        addRate("USD", "CAD", BigDecimal("1.36"))
        addRate("USD", "CHF", BigDecimal("0.88"))
        addRate("USD", "SEK", BigDecimal("10.42"))
        addRate("USD", "NZD", BigDecimal("1.63"))
        addRate("USD", "SGD", BigDecimal("1.34"))
        addRate("USD", "HKD", BigDecimal("7.81"))
        addRate("USD", "KRW", BigDecimal("1312.50"))
        addRate("USD", "MXN", BigDecimal("17.05"))
        addRate("USD", "BRL", BigDecimal("4.92"))
        addRate("USD", "ZAR", BigDecimal("18.35"))
        addRate("USD", "RUB", BigDecimal("91.50"))
        addRate("USD", "TRY", BigDecimal("32.25"))
        addRate("USD", "AED", BigDecimal("3.67"))
        addRate("USD", "MMK", BigDecimal("3900.00"))

        // EUR to other major currencies
        addRate("EUR", "GBP", BigDecimal("0.86"))
        addRate("EUR", "JPY", BigDecimal("162.50"))
        addRate("EUR", "CNY", BigDecimal("7.87"))
        addRate("EUR", "CHF", BigDecimal("0.96"))

        // GBP to other major currencies
        addRate("GBP", "JPY", BigDecimal("189.24"))
        addRate("GBP", "CHF", BigDecimal("1.11"))

        exchangeRateRepository.saveAll(rates)
        logger.info("${rates.size} exchange rates initialized successfully")
        logger.info("Exchange rates updated as of: $today")
    }
}