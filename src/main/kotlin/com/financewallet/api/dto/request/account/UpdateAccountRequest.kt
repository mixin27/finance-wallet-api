package com.financewallet.api.dto.request.account

import java.util.*

data class UpdateAccountRequest(
    val name: String? = null,
    val accountTypeId: UUID? = null,
    val currencyId: UUID? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isIncludedInTotal: Boolean? = null,
    val isActive: Boolean? = null
)