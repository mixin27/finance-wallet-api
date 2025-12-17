package com.financewallet.api.dto.request.transaction

import com.financewallet.api.entity.TransactionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class CreateTransactionRequest(
    @field:NotNull(message = "Account ID is required")
    val accountId: UUID,

    @field:NotNull(message = "Transaction type is required")
    val type: TransactionType, // INCOME, EXPENSE

    @field:NotNull(message = "Amount is required")
    val amount: BigDecimal,

    val categoryId: UUID? = null,

    @field:NotNull(message = "Transaction date is required")
    val transactionDate: LocalDateTime = LocalDateTime.now(),

    @field:NotBlank(message = "Description is required")
    val description: String,

    val note: String? = null,

    val payee: String? = null,

    val location: String? = null,

    val latitude: BigDecimal? = null,

    val longitude: BigDecimal? = null,

    val tags: List<String>? = null
)