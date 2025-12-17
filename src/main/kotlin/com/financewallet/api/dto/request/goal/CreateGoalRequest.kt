package com.financewallet.api.dto.request.goal

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CreateGoalRequest(
    @field:NotBlank(message = "Goal name is required")
    @field:Size(max = 200, message = "Goal name must not exceed 200 characters")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "Target amount is required")
    @field:DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    val targetAmount: BigDecimal,

    @field:DecimalMin(value = "0", message = "Initial amount cannot be negative")
    val initialAmount: BigDecimal? = BigDecimal.ZERO,

    val targetDate: LocalDate? = null,

    @field:NotNull(message = "Currency is required")
    val currencyId: UUID,

    val accountId: UUID? = null, // Optional: link to specific account

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format")
    val color: String? = null,

    @field:Size(max = 100, message = "Icon must not exceed 100 characters")
    val icon: String? = null
)