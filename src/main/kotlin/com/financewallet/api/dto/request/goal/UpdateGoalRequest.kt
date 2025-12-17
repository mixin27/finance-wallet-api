package com.financewallet.api.dto.request.goal

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateGoalRequest(
    @field:Size(max = 200, message = "Goal name must not exceed 200 characters")
    val name: String? = null,

    val description: String? = null,

    @field:DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    val targetAmount: BigDecimal? = null,

    val targetDate: LocalDate? = null,

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format")
    val color: String? = null,

    @field:Size(max = 100, message = "Icon must not exceed 100 characters")
    val icon: String? = null
)