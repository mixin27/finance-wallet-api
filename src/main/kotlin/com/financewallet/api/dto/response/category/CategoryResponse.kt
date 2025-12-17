package com.financewallet.api.dto.response.category

import java.time.LocalDateTime
import java.util.*

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val type: String,
    val color: String? = null,
    val icon: String? = null,
    val displayOrder: Int,
    val isSystem: Boolean,
    val isActive: Boolean,
    val parentCategoryId: UUID? = null,
    val parentCategoryName: String? = null,
    val subCategories: List<CategoryResponse> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)