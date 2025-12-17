package com.financewallet.api.controller

import com.financewallet.api.dto.request.account.CreateAccountRequest
import com.financewallet.api.dto.request.account.UpdateAccountRequest
import com.financewallet.api.dto.response.account.AccountResponse
import com.financewallet.api.dto.response.account.AccountSummaryResponse
import com.financewallet.api.dto.response.account.AccountTypeInfo
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.service.account.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping
    @Operation(summary = "Create Account", description = "Create a new account for the current user")
    fun createAccount(
        @Valid @RequestBody request: CreateAccountRequest
    ): ResponseEntity<ApiResponse<AccountResponse>> {
        val account = accountService.createAccount(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Account created successfully",
                data = account
            ))
    }

    @GetMapping
    @Operation(summary = "Get All Accounts", description = "Get all accounts for the current user")
    fun getAllAccounts(
        @RequestParam(defaultValue = "false") includeInactive: Boolean
    ): ResponseEntity<ApiResponse<List<AccountResponse>>> {
        val accounts = accountService.getAllAccounts(includeInactive)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Accounts retrieved successfully",
            data = accounts
        ))
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Account Summary", description = "Get account summary with statistics")
    fun getAccountSummary(): ResponseEntity<ApiResponse<AccountSummaryResponse>> {
        val summary = accountService.getAccountSummary()

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Account summary retrieved successfully",
            data = summary
        ))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Account by ID", description = "Get specific account details")
    fun getAccountById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<AccountResponse>> {
        val account = accountService.getAccountById(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Account retrieved successfully",
            data = account
        ))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Account", description = "Update account information")
    fun updateAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAccountRequest
    ): ResponseEntity<ApiResponse<AccountResponse>> {
        val account = accountService.updateAccount(id, request)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Account updated successfully",
            data = account
        ))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Account", description = "Delete account (soft delete)")
    fun deleteAccount(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        accountService.deleteAccount(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Account deleted successfully"
        ))
    }

    @GetMapping("/types")
    @Operation(summary = "Get Account Types", description = "Get all available account types")
    fun getAccountTypes(): ResponseEntity<ApiResponse<List<AccountTypeInfo>>> {
        val types = accountService.getAllAccountTypes()

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Account types retrieved successfully",
            data = types
        ))
    }
}