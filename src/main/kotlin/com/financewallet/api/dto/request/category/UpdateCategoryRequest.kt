package com.financewallet.api.dto.request.category

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateCategoryRequest(
    @field:Size(max = 100, message = "Category name must not exceed 100 characters")
    val name: String? = null,

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format. Use hex color code (e.g., #FF5722)")
    val color: String? = null,

    @field:Size(max = 100, message = "Icon must not exceed 100 characters")
    val icon: String? = null,

    val displayOrder: Int? = null,

    val isActive: Boolean? = null
)