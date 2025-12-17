package com.financewallet.api.dto.request.budget

import com.financewallet.api.entity.BudgetPeriod
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CreateBudgetRequest(
    @field:NotBlank(message = "Budget name is required")
    @field:Size(max = 200, message = "Budget name must not exceed 200 characters")
    val name: String,

    @field:NotNull(message = "Budget amount is required")
    @field:DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    val amount: BigDecimal,

    @field:NotNull(message = "Budget period is required")
    val period: BudgetPeriod,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    val endDate: LocalDate? = null, // Auto-calculated if not provided

    @field:NotNull(message = "Currency is required")
    val currencyId: UUID,

    val categoryId: UUID? = null, // If null, budget applies to all expenses

    @field:DecimalMin(value = "0", message = "Alert threshold must be between 0 and 100")
    @field:DecimalMax(value = "100", message = "Alert threshold must be between 0 and 100")
    val alertThreshold: BigDecimal = BigDecimal("80")
)