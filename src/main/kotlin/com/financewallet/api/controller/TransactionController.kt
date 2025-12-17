package com.financewallet.api.controller

import com.financewallet.api.dto.request.transaction.CreateTransactionRequest
import com.financewallet.api.dto.request.transaction.TransactionFilterRequest
import com.financewallet.api.dto.request.transaction.TransferRequest
import com.financewallet.api.dto.request.transaction.UpdateTransactionRequest
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.transaction.TransactionResponse
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.service.transaction.TransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping
    @Operation(summary = "Create Transaction", description = "Create income or expense transaction")
    fun createTransaction(
        @Valid @RequestBody request: CreateTransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.createTransaction(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Transaction created successfully",
                data = transaction
            ))
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer", description = "Transfer money between accounts")
    fun transfer(
        @Valid @RequestBody request: TransferRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.transfer(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Transfer completed successfully",
                data = transaction
            ))
    }

    @GetMapping
    @Operation(summary = "Get Transactions", description = "Get transactions with filters and pagination")
    fun getTransactions(
        @RequestParam(required = false) accountId: UUID?,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) type: TransactionType?,
        @RequestParam(required = false) startDate: LocalDateTime?,
        @RequestParam(required = false) endDate: LocalDateTime?,
        @RequestParam(required = false) minAmount: Double?,
        @RequestParam(required = false) maxAmount: Double?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "transactionDate") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<ApiResponse<Page<TransactionResponse>>> {
        val filter = TransactionFilterRequest(
            accountId = accountId,
            categoryId = categoryId,
            type = type,
            startDate = startDate,
            endDate = endDate,
            minAmount = minAmount,
            maxAmount = maxAmount,
            search = search,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val transactions = transactionService.getTransactions(filter)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Transactions retrieved successfully",
            data = transactions
        ))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Transaction by ID", description = "Get specific transaction details")
    fun getTransactionById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.getTransactionById(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Transaction retrieved successfully",
            data = transaction
        ))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Transaction", description = "Update transaction (not for transfers)")
    fun updateTransaction(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.updateTransaction(id, request)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Transaction updated successfully",
            data = transaction
        ))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Transaction", description = "Delete transaction and revert account balance")
    fun deleteTransaction(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        transactionService.deleteTransaction(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Transaction deleted successfully"
        ))
    }
}