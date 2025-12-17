package com.financewallet.api.dto.response.budget

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class BudgetResponse(
    val id: UUID,
    val name: String,
    val amount: BigDecimal,
    val period: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val alertThreshold: BigDecimal,
    val isActive: Boolean,
    val categoryId: UUID?,
    val categoryName: String?,
    val currencyId: UUID,
    val currencyCode: String,
    val currencySymbol: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)