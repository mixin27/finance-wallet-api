package com.financewallet.api.controller

import com.financewallet.api.dto.request.budget.CreateBudgetRequest
import com.financewallet.api.dto.request.budget.UpdateBudgetRequest
import com.financewallet.api.dto.response.budget.BudgetProgressResponse
import com.financewallet.api.dto.response.budget.BudgetResponse
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.service.budget.BudgetService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/budgets")
@Tag(name = "Budgets", description = "Budget management endpoints")
class BudgetController(
    private val budgetService: BudgetService
) {

    /**
     * Get all budgets
     * GET /api/budgets
     * GET /api/budgets?activeOnly=true
     */
    @GetMapping
    fun getAllBudgets(
        @RequestParam(defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<ApiResponse<List<BudgetResponse>>> {
        val budgets = budgetService.getAllBudgets(activeOnly)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = budgets,
                message = "Budgets retrieved successfully"
            )
        )
    }

    /**
     * Get active budgets with progress
     * GET /api/budgets/active
     */
    @GetMapping("/active")
    fun getActiveBudgets(): ResponseEntity<ApiResponse<List<BudgetProgressResponse>>> {
        val budgets = budgetService.getActiveBudgets()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = budgets,
                message = "Active budgets with progress retrieved successfully"
            )
        )
    }

    /**
     * Get budget by ID
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    fun getBudgetById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<BudgetResponse>> {
        val budget = budgetService.getBudgetById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = budget,
                message = "Budget retrieved successfully"
            )
        )
    }

    /**
     * Create budget
     * POST /api/budgets
     */
    @PostMapping
    fun createBudget(
        @Valid @RequestBody request: CreateBudgetRequest
    ): ResponseEntity<ApiResponse<BudgetResponse>> {
        val budget = budgetService.createBudget(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = budget,
                message = "Budget created successfully"
            )
        )
    }

    /**
     * Update budget
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    fun updateBudget(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBudgetRequest
    ): ResponseEntity<ApiResponse<BudgetResponse>> {
        val budget = budgetService.updateBudget(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = budget,
                message = "Budget updated successfully"
            )
        )
    }

    /**
     * Delete budget (soft delete)
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteBudget(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        budgetService.deleteBudget(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = Unit,
                message = "Budget deleted successfully"
            )
        )
    }
}