package com.financewallet.api.dto.response.recurring

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class RecurringTransactionResponse(
    val id: UUID,
    val accountId: UUID,
    val accountName: String,
    val toAccountId: UUID?,
    val toAccountName: String?,
    val categoryId: UUID?,
    val categoryName: String?,
    val type: String,
    val amount: BigDecimal,
    val currencyCode: String,
    val currencySymbol: String,
    val description: String?,
    val frequency: String,
    val intervalValue: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val nextOccurrenceDate: LocalDate,
    val lastGeneratedDate: LocalDate?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)