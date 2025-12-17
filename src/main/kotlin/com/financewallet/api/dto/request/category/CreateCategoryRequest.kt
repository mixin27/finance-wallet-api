package com.financewallet.api.dto.request.category

import com.financewallet.api.entity.CategoryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

data class CreateCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 100, message = "Category name must not exceed 100 characters")
    val name: String,

    @field:NotNull(message = "Category type is required")
    val type: CategoryType,

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format. Use hex color code (e.g., #FF5722)")
    val color: String? = null,

    @field:Size(max = 100, message = "Icon must not exceed 100 characters")
    val icon: String? = null,

    val parentCategoryId: UUID? = null
)