package com.financewallet.api.dto.response.dashboard

import com.financewallet.api.dto.response.account.CurrencyInfo
import java.math.BigDecimal
import java.time.LocalDate

data class StatisticsResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netIncome: BigDecimal,
    val avgDailyIncome: BigDecimal,
    val avgDailyExpense: BigDecimal,
    val expensesByCategory: List<CategoryBreakdown>,
    val incomeByCategory: List<CategoryBreakdown>,
    val dailyTrends: List<DailyTrend>,
    val defaultCurrency: CurrencyInfo
)

data class DailyTrend(
    val date: LocalDate,
    val income: BigDecimal,
    val expenses: BigDecimal,
    val net: BigDecimal
)