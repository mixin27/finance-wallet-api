package com.financewallet.api.dto.response.common

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val validationErrors: Map<String, String>? = null
)