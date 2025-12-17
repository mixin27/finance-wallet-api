package com.financewallet.api.service.dashboard

import com.financewallet.api.dto.response.dashboard.*
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.BudgetRepository
import com.financewallet.api.repository.TransactionRepository
import com.financewallet.api.service.auth.AuthService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

@Service
class DashboardService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val authService: AuthService
) {
    /**
     * Get complete dashboard data
     */
    @Transactional(readOnly = true)
    fun getDashboard(): DashboardResponse {
        val currentUser = authService.getCurrentUser()
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        // Current month dates
        val firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        val startOfMonth = firstDayOfMonth.atStartOfDay()
        val endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX)

        // Get total balance from all accounts
        val totalBalance = BigDecimal.valueOf(
            accountRepository.getTotalBalance(currentUser)
        )

        // Get this month's income and expenses
        val monthIncome = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.INCOME,
                startOfMonth,
                endOfMonth
            )
        )

        val monthExpenses = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.EXPENSE,
                startOfMonth,
                endOfMonth
            )
        )

        // Calculate savings
        val savings = monthIncome - monthExpenses

        // Get previous month for comparison
        val previousMonth = firstDayOfMonth.minusMonths(1)
        val prevMonthStart = previousMonth.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        val prevMonthEnd = previousMonth.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)

        val prevMonthIncome = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.INCOME,
                prevMonthStart,
                prevMonthEnd
            )
        )

        val prevMonthExpenses = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.EXPENSE,
                prevMonthStart,
                prevMonthEnd
            )
        )

        // Calculate percentage changes
        val incomeChange = calculatePercentageChange(prevMonthIncome, monthIncome)
        val expenseChange = calculatePercentageChange(prevMonthExpenses, monthExpenses)

        // Get top spending categories
        val categoryBreakdown = getExpensesByCategory(startOfMonth, endOfMonth)

        // Get recent transactions (last 10)
        val recentTransactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startOfMonth,
            now,
            org.springframework.data.domain.PageRequest.of(0, 10)
        ).content

        // Get active budgets
        val activeBudgets = budgetRepository.findActiveBudgetsForDate(currentUser, today)

        return DashboardResponse(
            totalBalance = totalBalance,
            monthIncome = monthIncome,
            monthExpenses = monthExpenses,
            savings = savings,
            incomeChange = incomeChange,
            expenseChange = expenseChange,
            categoryBreakdown = categoryBreakdown,
            recentTransactionsCount = recentTransactions.size,
            activeBudgetsCount = activeBudgets.size,
            currentMonth = YearMonth.now().toString()
        )
    }

    /**
     * Get detailed statistics for a date range
     */
    @Transactional(readOnly = true)
    fun getStatistics(startDate: LocalDate, endDate: LocalDate): StatisticsResponse {
        val currentUser = authService.getCurrentUser()

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(LocalTime.MAX)

        // Get totals
        val totalIncome = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.INCOME,
                startDateTime,
                endDateTime
            )
        )

        val totalExpenses = BigDecimal.valueOf(
            transactionRepository.sumByUserAndTypeAndDateRange(
                currentUser,
                TransactionType.EXPENSE,
                startDateTime,
                endDateTime
            )
        )

        val netIncome = totalIncome - totalExpenses

        // Get category breakdown
        val expensesByCategory = getExpensesByCategory(startDateTime, endDateTime)
        val incomeByCategory = getIncomeByCategory(startDateTime, endDateTime)

        // Get daily trends (for charts)
        val dailyData = getDailyTrends(startDate, endDate)

        // Calculate average per day
        val dayCount = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val avgDailyIncome = if (dayCount > 0) totalIncome.divide(BigDecimal(dayCount), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
        val avgDailyExpense = if (dayCount > 0) totalExpenses.divide(BigDecimal(dayCount), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO

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
            dailyTrends = dailyData
        )
    }

    /**
     * Get expenses grouped by category
     */
    private fun getExpensesByCategory(startDate: LocalDateTime, endDate: LocalDateTime): List<CategoryBreakdown> {
        val currentUser = authService.getCurrentUser()

        val results = transactionRepository.getExpensesByCategory(currentUser, startDate, endDate)

        return results.map { result ->
            val category = result[0] as? com.financewallet.api.entity.Category
            val amount = BigDecimal.valueOf(result[1] as Double)

            CategoryBreakdown(
                categoryId = category?.id,
                categoryName = category?.name ?: "Uncategorized",
                amount = amount,
                color = category?.color,
                icon = category?.icon
            )
        }.sortedByDescending { it.amount }
    }

    /**
     * Get income grouped by category
     */
    private fun getIncomeByCategory(startDate: LocalDateTime, endDate: LocalDateTime): List<CategoryBreakdown> {
        val currentUser = authService.getCurrentUser()

        val transactions = transactionRepository.findByUserAndDateRange(
            currentUser,
            startDate,
            endDate,
            org.springframework.data.domain.Pageable.unpaged()
        ).content.filter { it.type == TransactionType.INCOME }

        return transactions
            .groupBy { it.category }
            .map { (category, txns) ->
                CategoryBreakdown(
                    categoryId = category?.id,
                    categoryName = category?.name ?: "Uncategorized",
                    amount = txns.sumOf { it.amount },
                    color = category?.color,
                    icon = category?.icon
                )
            }
            .sortedByDescending { it.amount }
    }

    /**
     * Get daily income/expense trends
     */
    private fun getDailyTrends(startDate: LocalDate, endDate: LocalDate): List<DailyTrend> {
        val currentUser = authService.getCurrentUser()
        val trends = mutableListOf<DailyTrend>()

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dayStart = currentDate.atStartOfDay()
            val dayEnd = currentDate.atTime(LocalTime.MAX)

            val income = BigDecimal.valueOf(
                transactionRepository.sumByUserAndTypeAndDateRange(
                    currentUser,
                    TransactionType.INCOME,
                    dayStart,
                    dayEnd
                )
            )

            val expenses = BigDecimal.valueOf(
                transactionRepository.sumByUserAndTypeAndDateRange(
                    currentUser,
                    TransactionType.EXPENSE,
                    dayStart,
                    dayEnd
                )
            )

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