package com.financewallet.api.dto.response.goal

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class GoalResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val remaining: BigDecimal,
    val percentageComplete: BigDecimal,
    val targetDate: LocalDate?,
    val color: String?,
    val icon: String?,
    val isCompleted: Boolean,
    val accountId: UUID?,
    val accountName: String?,
    val currencyId: UUID,
    val currencyCode: String,
    val currencySymbol: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)