package com.financewallet.api.dto.response.account

import com.financewallet.api.entity.Currency
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class AccountResponse(
    val id: UUID,
    val name: String,
    val accountType: AccountTypeInfo,
    val currency: CurrencyInfo,
    val description: String?,
    val initialBalance: BigDecimal,
    val currentBalance: BigDecimal,
    val balanceInDefaultCurrency: BigDecimal,
    val color: String?,
    val icon: String?,
    val isIncludedInTotal: Boolean,
    val displayOrder: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AccountTypeInfo(
    val id: UUID,
    val name: String,
    val icon: String?
)

data class CurrencyInfo(
    val id: UUID,
    val code: String,
    val symbol: String,
    val name: String
)