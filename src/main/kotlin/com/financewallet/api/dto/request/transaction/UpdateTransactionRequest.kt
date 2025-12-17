package com.financewallet.api.dto.request.transaction

import com.financewallet.api.entity.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class UpdateTransactionRequest(
    val amount: BigDecimal? = null,
    val categoryId: UUID? = null,
    val transactionDate: LocalDateTime? = null,
    val description: String? = null,
    val note: String? = null,
    val payee: String? = null,
    val location: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val tags: List<String>? = null
)

data class TransactionFilterRequest(
    val accountId: UUID? = null,
    val categoryId: UUID? = null,
    val type: TransactionType? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val search: String? = null, // Search in description, note, payee
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "transactionDate", // transactionDate, amount, createdAt
    val sortDirection: String = "DESC" // ASC, DESC
)