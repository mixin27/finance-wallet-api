package com.financewallet.api.dto.request.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)