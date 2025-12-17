package com.financewallet.api.dto.request.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val newPassword: String
)