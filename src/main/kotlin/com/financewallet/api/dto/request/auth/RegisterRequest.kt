package com.financewallet.api.dto.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String,

    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    val phoneNumber: String? = null,

    val defaultCurrencyId: UUID? = null
)