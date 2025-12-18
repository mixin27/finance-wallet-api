package com.financewallet.api.controller

import com.financewallet.api.dto.request.recurring.CreateRecurringTransactionRequest
import com.financewallet.api.dto.request.recurring.UpdateRecurringTransactionRequest
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.recurring.RecurringTransactionResponse
import com.financewallet.api.service.recurring.RecurringTransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/recurring-transactions")
class RecurringTransactionController(
    private val recurringTransactionService: RecurringTransactionService
) {

    /**
     * Get all recurring transactions
     * GET /api/recurring-transactions
     * GET /api/recurring-transactions?activeOnly=true
     */
    @GetMapping
    fun getAllRecurringTransactions(
        @RequestParam(defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> {
        val transactions = recurringTransactionService.getAllRecurringTransactions(activeOnly)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = transactions,
                message = "Recurring transactions retrieved successfully"
            )
        )
    }

    /**
     * Get recurring transaction by ID
     * GET /api/recurring-transactions/{id}
     */
    @GetMapping("/{id}")
    fun getRecurringTransactionById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<RecurringTransactionResponse>> {
        val transaction = recurringTransactionService.getRecurringTransactionById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = transaction,
                message = "Recurring transaction retrieved successfully"
            )
        )
    }

    /**
     * Create recurring transaction
     * POST /api/recurring-transactions
     */
    @PostMapping
    fun createRecurringTransaction(
        @Valid @RequestBody request: CreateRecurringTransactionRequest
    ): ResponseEntity<ApiResponse<RecurringTransactionResponse>> {
        val transaction = recurringTransactionService.createRecurringTransaction(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = transaction,
                message = "Recurring transaction created successfully"
            )
        )
    }

    /**
     * Update recurring transaction
     * PUT /api/recurring-transactions/{id}
     */
    @PutMapping("/{id}")
    fun updateRecurringTransaction(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateRecurringTransactionRequest
    ): ResponseEntity<ApiResponse<RecurringTransactionResponse>> {
        val transaction = recurringTransactionService.updateRecurringTransaction(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = transaction,
                message = "Recurring transaction updated successfully"
            )
        )
    }

    /**
     * Delete recurring transaction
     * DELETE /api/recurring-transactions/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteRecurringTransaction(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        recurringTransactionService.deleteRecurringTransaction(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = Unit,
                message = "Recurring transaction deleted successfully"
            )
        )
    }
}