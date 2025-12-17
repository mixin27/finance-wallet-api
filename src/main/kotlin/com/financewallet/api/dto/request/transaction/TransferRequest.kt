package com.financewallet.api.dto.request.transaction

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TransferRequest(
    @field:NotNull(message = "From account ID is required")
    val fromAccountId: UUID,

    @field:NotNull(message = "To account ID is required")
    val toAccountId: UUID,

    @field:NotNull(message = "Amount is required")
    val amount: BigDecimal,

    val exchangeRate: BigDecimal? = null, // Required if different currencies

    @field:NotNull(message = "Transaction date is required")
    val transactionDate: LocalDateTime = LocalDateTime.now(),

    @field:NotBlank(message = "Description is required")
    val description: String,

    val note: String? = null
)