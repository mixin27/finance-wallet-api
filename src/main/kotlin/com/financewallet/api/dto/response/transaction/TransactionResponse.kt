package com.financewallet.api.dto.response.transaction

import com.financewallet.api.dto.response.account.AccountTypeInfo
import com.financewallet.api.dto.response.account.CurrencyInfo
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TransactionResponse(
    val id: UUID,
    val account: TransactionAccountInfo,
    val toAccount: TransactionAccountInfo? = null, // For transfers
    val category: CategoryInfo?,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val amount: BigDecimal,
    val currency: CurrencyInfo,
    val exchangeRate: BigDecimal?,
    val convertedAmount: BigDecimal?, // For transfers with different currencies
    val transactionDate: LocalDateTime,
    val description: String,
    val note: String?,
    val payee: String?,
    val location: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val status: String,
    val tags: List<TagInfo>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class TransactionAccountInfo(
    val id: UUID,
    val name: String,
    val accountType: AccountTypeInfo,
    val currency: CurrencyInfo
)

data class CategoryInfo(
    val id: UUID,
    val name: String,
    val type: String,
    val color: String?,
    val icon: String?
)

data class TagInfo(
    val id: UUID,
    val name: String,
    val color: String?
)