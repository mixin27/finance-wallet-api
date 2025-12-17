package com.financewallet.api.controller

import com.financewallet.api.dto.request.category.CreateCategoryRequest
import com.financewallet.api.dto.request.category.UpdateCategoryRequest
import com.financewallet.api.dto.response.category.CategoryResponse
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.entity.CategoryType
import com.financewallet.api.service.category.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {
    /**
     * Get all categories (system + user custom)
     * GET /api/categories
     * GET /api/categories?type=INCOME
     * GET /api/categories?type=EXPENSE
     */
    @GetMapping
    fun getAllCategories(
        @RequestParam(required = false) type: CategoryType?
    ): ResponseEntity<ApiResponse<List<CategoryResponse>>> {
        val categories = categoryService.getAllCategories(type)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = categories,
                message = "Categories retrieved successfully"
            )
        )
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.getCategoryById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = category,
                message = "Category retrieved successfully"
            )
        )
    }

    /**
     * Create custom category
     * POST /api/categories
     */
    @PostMapping
    fun createCategory(
        @Valid @RequestBody request: CreateCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = category,
                message = "Category created successfully"
            )
        )
    }

    /**
     * Update custom category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.updateCategory(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = category,
                message = "Category updated successfully"
            )
        )
    }

    /**
     * Delete custom category (soft delete)
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteCategory(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        categoryService.deleteCategory(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = Unit,
                message = "Category deleted successfully"
            )
        )
    }
}