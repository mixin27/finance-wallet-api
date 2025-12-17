package com.financewallet.api.dto.response.currency

import java.util.*

data class CurrencyResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Short,
    val isActive: Boolean
)