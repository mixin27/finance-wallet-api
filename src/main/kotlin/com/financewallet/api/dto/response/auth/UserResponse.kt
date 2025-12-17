package com.financewallet.api.dto.response.auth

import java.time.LocalDateTime
import java.util.*

data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val fullName: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val isEmailVerified: Boolean,
    val authProvider: String,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)