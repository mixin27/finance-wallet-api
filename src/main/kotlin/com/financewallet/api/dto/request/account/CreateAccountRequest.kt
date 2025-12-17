package com.financewallet.api.dto.request.account

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*

data class CreateAccountRequest(
    @field:NotBlank(message = "Account name is required")
    val name: String,

    @field:NotNull(message = "Account type ID is required")
    val accountTypeId: UUID,

    @field:NotNull(message = "Currency ID is required")
    val currencyId: UUID,

    val description: String? = null,

    @field:NotNull(message = "Initial balance is required")
    val initialBalance: BigDecimal = BigDecimal.ZERO,

    val color: String? = null, // Hex color: #FF5733

    val icon: String? = null,

    val isIncludedInTotal: Boolean = true
)