package com.financewallet.api.dto.response.dashboard

import java.math.BigDecimal
import java.util.*

data class DashboardResponse(
    val totalBalance: BigDecimal,
    val monthIncome: BigDecimal,
    val monthExpenses: BigDecimal,
    val savings: BigDecimal,
    val incomeChange: BigDecimal, // Percentage change from last month
    val expenseChange: BigDecimal, // Percentage change from last month
    val categoryBreakdown: List<CategoryBreakdown>,
    val recentTransactionsCount: Int,
    val activeBudgetsCount: Int,
    val currentMonth: String // e.g., "2024-01"
)

data class CategoryBreakdown(
    val categoryId: UUID?,
    val categoryName: String,
    val amount: BigDecimal,
    val color: String?,
    val icon: String?
)