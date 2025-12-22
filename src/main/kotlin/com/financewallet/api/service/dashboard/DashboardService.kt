package com.financewallet.api.service.dashboard

import com.financewallet.api.dto.response.account.CurrencyInfo
import com.financewallet.api.dto.response.dashboard.*
import com.financewallet.api.entity.Currency
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.entity.User
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.BudgetRepository
import com.financewallet.api.repository.ExchangeRateRepository
import com.financewallet.api.repository.TransactionRepository
import com.financewallet.api.service.auth.AuthService
import com.financewallet.api.service.user.UserPreferenceService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters


@Service
class DashboardService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val authService: AuthService,
    private val userPreferenceService: UserPreferenceService
) {
//    private val logger = LoggerFactory.getLogger(DashboardService::class.java)

    /**
     * Get complete dashboard data
     */
    /**
     * Get complete dashboard data with proper currency conversion
     */
    @Transactional(readOnly = true)
    fun getDashboard(): DashboardResponse {
        val currentUser = authService.getCurrentUser()

        // Get user preferences
        val preferences = userPreferenceService.getUserPreferences()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

        val userTimezone = ZoneId.of(preferences.timezone)

        // Get current date in user's timezone
        val now = ZonedDateTime.now(userTimezone)
        val today = now.toLocalDate()

        // Get all active accounts with currency conversion
        val accounts = accountRepository.findByUserAndIsActiveTrue(currentUser)
        val accountBalances = accounts
            .filter { it.isIncludedInTotal }
            .map { account ->
                val balanceInDefaultCurrency = convertToDefaultCurrency(
                    amount = account.currentBalance,
                    fromCurrency = account.currency,
                    toCurrency = defaultCurrency
                )

                AccountBalanceInfo(
                    accountId = account.id!!,
                    accountName = account.name,
                    balance = account.currentBalance,
                    currency = CurrencyInfo(
                        id = account.currency.id!!,
                        code = account.currency.code,
                        symbol = account.currency.symbol,
                        name = account.currency.name
                    ),
                    balanceInDefaultCurrency = balanceInDefaultCurrency
                )
            }

        val totalBalance = accountBalances.sumOf { it.balanceInDefaultCurrency }

        // Current month date range in user's timezone
        val firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        val startOfMonth = firstDayOfMonth.atStartOfDay(userTimezone).toLocalDateTime()
        val endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX)

        // Get transactions and convert to default currency
        val transactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startOfMonth,
            endOfMonth,
            Pageable.unpaged()
        ).content

        val monthIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val monthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val savings = monthIncome - monthExpenses

        // Previous month comparison
        val previousMonth = firstDayOfMonth.minusMonths(1)
        val prevMonthStart = previousMonth.with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay(userTimezone).toLocalDateTime()
        val prevMonthEnd = previousMonth.with(TemporalAdjusters.lastDayOfMonth())
            .atTime(LocalTime.MAX)

        val prevTransactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            prevMonthStart,
            prevMonthEnd,
            Pageable.unpaged()
        ).content

        val prevMonthIncome = prevTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val prevMonthExpenses = prevTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val incomeChange = calculatePercentageChange(prevMonthIncome, monthIncome)
        val expenseChange = calculatePercentageChange(prevMonthExpenses, monthExpenses)

        // Category breakdown with currency conversion
        val categoryBreakdown = getExpensesByCategory(startOfMonth, endOfMonth, defaultCurrency, currentUser)

        // Active budgets
        val activeBudgets = budgetRepository.findActiveBudgetsForDate(currentUser, today)

        return DashboardResponse(
            totalBalance = totalBalance,
            defaultCurrency = CurrencyInfo(
                id = defaultCurrency.id!!,
                code = defaultCurrency.code,
                symbol = defaultCurrency.symbol,
                name = defaultCurrency.name
            ),
            accountBalances = accountBalances,
            monthIncome = monthIncome,
            monthExpenses = monthExpenses,
            savings = savings,
            incomeChange = incomeChange,
            expenseChange = expenseChange,
            categoryBreakdown = categoryBreakdown,
            recentTransactionsCount = transactions.size,
            activeBudgetsCount = activeBudgets.size,
            currentMonth = YearMonth.now().toString()
        )
    }

    /**
     * Get detailed statistics for date range
     */
    @Transactional(readOnly = true)
    fun getStatistics(startDate: LocalDate, endDate: LocalDate): StatisticsResponse {
        val currentUser = authService.getCurrentUser()

        // Get user preferences for currency and timezone
        val preferences = userPreferenceService.getUserPreferences()
        val defaultCurrency = userPreferenceService.getDefaultCurrency()

        val userTimezone = ZoneId.of(preferences.timezone)

        val startDateTime = startDate.atStartOfDay(userTimezone).toLocalDateTime()
        val endDateTime = endDate.atTime(LocalTime.MAX)

        // Get all transactions and convert to default currency
        val transactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startDateTime,
            endDateTime,
            Pageable.unpaged()
        ).content

        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

        val netIncome = totalIncome - totalExpenses

        // Category breakdowns
        val expensesByCategory = getExpensesByCategory(startDateTime, endDateTime, defaultCurrency, currentUser)
        val incomeByCategory = getIncomeByCategory(startDateTime, endDateTime, defaultCurrency)

        // Daily trends
        val dailyData = getDailyTrends(startDate, endDate, defaultCurrency, userTimezone)

        // Calculate averages
        val dayCount = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val avgDailyIncome = if (dayCount > 0) {
            totalIncome.divide(BigDecimal(dayCount), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        val avgDailyExpense = if (dayCount > 0) {
            totalExpenses.divide(BigDecimal(dayCount), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return StatisticsResponse(
            startDate = startDate,
            endDate = endDate,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netIncome = netIncome,
            avgDailyIncome = avgDailyIncome,
            avgDailyExpense = avgDailyExpense,
            expensesByCategory = expensesByCategory,
            incomeByCategory = incomeByCategory,
            dailyTrends = dailyData,
            defaultCurrency = CurrencyInfo(
                id = defaultCurrency.id!!,
                code = defaultCurrency.code,
                symbol = defaultCurrency.symbol,
                name = defaultCurrency.name
            )
        )
    }

    /**
     * Convert amount to default currency using exchange rates
     */
    private fun convertToDefaultCurrency(
        amount: BigDecimal,
        fromCurrency: Currency,
        toCurrency: Currency
    ): BigDecimal {
        if (fromCurrency.id == toCurrency.id) {
            return amount
        }

        val exchangeRate = exchangeRateRepository.findLatestRate(
            fromCurrency,
            toCurrency,
            LocalDate.now()
        )

        return if (exchangeRate != null) {
            amount.multiply(exchangeRate.rate).setScale(2, RoundingMode.HALF_UP)
        } else {
//            logger.warn(
//                "No exchange rate found for ${fromCurrency.code} to ${toCurrency.code}. " +
//                        "Using 1:1 conversion. Please add exchange rates!"
//            )
            amount
        }
    }

    /**
     * Get expenses grouped by category with currency conversion
     */
    private fun getExpensesByCategory(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        defaultCurrency: Currency,
        currentUser: User,
    ): List<CategoryBreakdown> {
        val transactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startDate,
            endDate,
            Pageable.unpaged()
        ).content.filter { it.type == TransactionType.EXPENSE }

        return transactions
            .groupBy { it.category }
            .map { (category, txns) ->
                val originalAmount = txns.sumOf { it.amount }
                val convertedAmount = txns.sumOf {
                    convertToDefaultCurrency(it.amount, it.currency, defaultCurrency)
                }

                CategoryBreakdown(
                    categoryId = category?.id,
                    categoryName = category?.name ?: "Uncategorized",
                    amount = originalAmount,
                    amountInDefaultCurrency = convertedAmount,
                    color = category?.color,
                    icon = category?.icon
                )
            }
            .sortedByDescending { it.amountInDefaultCurrency }
    }

    /**
     * Get income grouped by category with currency conversion
     */
    private fun getIncomeByCategory(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        defaultCurrency: Currency
    ): List<CategoryBreakdown> {
        val currentUser = authService.getCurrentUser()

        val transactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startDate,
            endDate,
            Pageable.unpaged()
        ).content.filter { it.type == TransactionType.INCOME }

        return transactions
            .groupBy { it.category }
            .map { (category, txns) ->
                val originalAmount = txns.sumOf { it.amount }
                val convertedAmount = txns.sumOf {
                    convertToDefaultCurrency(it.amount, it.currency, defaultCurrency)
                }

                CategoryBreakdown(
                    categoryId = category?.id,
                    categoryName = category?.name ?: "Uncategorized",
                    amount = originalAmount,
                    amountInDefaultCurrency = convertedAmount,
                    color = category?.color,
                    icon = category?.icon
                )
            }
            .sortedByDescending { it.amountInDefaultCurrency }
    }

    /**
     * Get daily trends with currency conversion and timezone support
     */
    private fun getDailyTrends(
        startDate: LocalDate,
        endDate: LocalDate,
        defaultCurrency: Currency,
        userTimezone: ZoneId
    ): List<DailyTrend> {
        val currentUser = authService.getCurrentUser()
        val trends = mutableListOf<DailyTrend>()

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dayStart = currentDate.atStartOfDay(userTimezone).toLocalDateTime()
            val dayEnd = currentDate.atTime(LocalTime.MAX)

            val dayTransactions = transactionRepository.findByUserAndDateRange(
                currentUser,
                dayStart,
                dayEnd,
                Pageable.unpaged()
            ).content

            val income = dayTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

            val expenses = dayTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { convertToDefaultCurrency(it.amount, it.currency, defaultCurrency) }

            trends.add(
                DailyTrend(
                    date = currentDate,
                    income = income,
                    expenses = expenses,
                    net = income - expenses
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return trends
    }

    /**
     * Calculate percentage change
     */
    private fun calculatePercentageChange(oldValue: BigDecimal, newValue: BigDecimal): BigDecimal {
        if (oldValue.toBigInteger() == BigInteger.ZERO) {
            return if (newValue > BigDecimal.ZERO) BigDecimal(100) else BigDecimal.ZERO
        }

        return ((newValue - oldValue)
            .divide(oldValue, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100)))
            .setScale(2, RoundingMode.HALF_UP)
    }
}