package com.financewallet.api.dto.response.account

import java.math.BigDecimal

data class AccountSummaryResponse(
    val totalAccounts: Int,
    val activeAccounts: Int,
    val totalBalance: BigDecimal,
    val balanceByCurrency: List<CurrencyBalance>,
    val accounts: List<AccountResponse>
)

data class CurrencyBalance(
    val currencyCode: String,
    val currencySymbol: String,
    val balance: BigDecimal,
    val accountCount: Int
)