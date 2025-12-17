package com.financewallet.api.dto.response.common

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
)