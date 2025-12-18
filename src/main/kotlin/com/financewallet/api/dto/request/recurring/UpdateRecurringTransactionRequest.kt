package com.financewallet.api.dto.request.recurring

import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateRecurringTransactionRequest(
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    val amount: BigDecimal? = null,

    val description: String? = null,

    val endDate: LocalDate? = null,

    val isActive: Boolean? = null
)