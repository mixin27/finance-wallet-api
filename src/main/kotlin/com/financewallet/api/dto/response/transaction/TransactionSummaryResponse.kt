package com.financewallet.api.dto.response.transaction

import java.math.BigDecimal
import java.time.LocalDate

data class TransactionSummaryResponse(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val netAmount: BigDecimal, // income - expense
    val transactionCount: Int,
    val period: PeriodInfo,
    val expensesByCategory: List<CategoryExpense>,
    val incomesByCategory: List<CategoryIncome>
)

data class PeriodInfo(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodType: String // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
)

data class CategoryExpense(
    val category: CategoryInfo?,
    val amount: BigDecimal,
    val percentage: Double,
    val transactionCount: Int
)

data class CategoryIncome(
    val category: CategoryInfo?,
    val amount: BigDecimal,
    val percentage: Double,
    val transactionCount: Int
)
