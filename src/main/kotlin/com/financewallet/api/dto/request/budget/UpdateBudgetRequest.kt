package com.financewallet.api.dto.request.budget

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateBudgetRequest(
    @field:Size(max = 200, message = "Budget name must not exceed 200 characters")
    val name: String? = null,

    @field:DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    val amount: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Alert threshold must be between 0 and 100")
    @field:DecimalMax(value = "100", message = "Alert threshold must be between 0 and 100")
    val alertThreshold: BigDecimal? = null,

    val isActive: Boolean? = null
)