package com.financewallet.api.dto.request.user

import com.financewallet.api.entity.Theme
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

data class UpdateUserPreferenceRequest(
    val defaultCurrencyId: UUID? = null,

    @field:Size(min = 2, max = 10, message = "Language code must be 2-10 characters")
    @field:Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Invalid language code format (e.g., en, en-US)")
    val language: String? = null,

    @field:Size(max = 20, message = "Date format must not exceed 20 characters")
    val dateFormat: String? = null,

    @field:Min(value = 0, message = "First day of week must be between 0 (Sunday) and 6 (Saturday)")
    @field:Max(value = 6, message = "First day of week must be between 0 (Sunday) and 6 (Saturday)")
    val firstDayOfWeek: Short? = null,

    @field:Pattern(
        regexp = "^(UTC|GMT|[A-Z][a-z]+/[A-Z][a-z_]+)$",
        message = "Invalid timezone format (e.g., UTC, America/New_York, Europe/London)"
    )
    val timezone: String? = null,

    val theme: Theme? = null,

    val enableNotifications: Boolean? = null,

    val enableBiometric: Boolean? = null,

    val autoBackup: Boolean? = null
)