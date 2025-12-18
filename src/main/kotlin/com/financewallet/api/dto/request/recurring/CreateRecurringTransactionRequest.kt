package com.financewallet.api.dto.request.recurring

import com.financewallet.api.entity.RecurringFrequency
import com.financewallet.api.entity.TransactionType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CreateRecurringTransactionRequest(
    @field:NotNull(message = "Account is required")
    val accountId: UUID,

    val toAccountId: UUID? = null, // Required for TRANSFER type

    val categoryId: UUID? = null,

    @field:NotNull(message = "Transaction type is required")
    val type: TransactionType,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    val amount: BigDecimal,

    val description: String? = null,

    @field:NotNull(message = "Frequency is required")
    val frequency: RecurringFrequency,

    @field:Min(value = 1, message = "Interval value must be at least 1")
    val intervalValue: Int = 1,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    val endDate: LocalDate? = null
)