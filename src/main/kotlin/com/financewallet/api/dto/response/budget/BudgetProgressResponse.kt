package com.financewallet.api.dto.response.budget

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class BudgetProgressResponse(
    val id: UUID,
    val name: String,
    val amount: BigDecimal,
    val spent: BigDecimal,
    val remaining: BigDecimal,
    val percentageUsed: BigDecimal,
    val period: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val alertThreshold: BigDecimal,
    val isOverBudget: Boolean,
    val isNearLimit: Boolean,
    val categoryName: String?,
    val currencyCode: String,
    val currencySymbol: String
)