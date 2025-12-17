package com.financewallet.api.dto.response.file

import java.time.LocalDateTime
import java.util.UUID

data class AttachmentResponse(
    val id: UUID,
    val fileName: String,
    val originalFileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val fileUrl: String,
    val thumbnailUrl: String?,
    val createdAt: LocalDateTime
)