package com.financewallet.api.dto.response.user

import java.time.LocalDateTime
import java.util.*

data class UserPreferenceResponse(
    val userId: UUID,
    val defaultCurrencyId: UUID?,
    val defaultCurrencyCode: String?,
    val defaultCurrencySymbol: String?,
    val language: String,
    val dateFormat: String,
    val firstDayOfWeek: Short,
    val theme: String,
    val enableNotifications: Boolean,
    val enableBiometric: Boolean,
    val autoBackup: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)