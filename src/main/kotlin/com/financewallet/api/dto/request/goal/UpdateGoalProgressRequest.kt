package com.financewallet.api.dto.request.goal

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdateGoalProgressRequest(
    @field:NotNull(message = "Amount is required")
    val amount: BigDecimal // Positive to add, negative to subtract
)