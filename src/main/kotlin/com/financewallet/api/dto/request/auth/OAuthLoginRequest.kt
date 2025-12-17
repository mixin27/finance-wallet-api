package com.financewallet.api.dto.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.util.*

data class OAuthLoginRequest(
    @field:NotBlank(message = "Provider is required")
    val provider: String, // GOOGLE, APPLE

    @field:NotBlank(message = "Provider ID is required")
    val providerId: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    val profileImageUrl: String? = null,

    val defaultCurrencyId: UUID? = null
)