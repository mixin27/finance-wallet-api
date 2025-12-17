package com.financewallet.api.controller

import com.financewallet.api.dto.request.auth.ChangePasswordRequest
import com.financewallet.api.dto.request.auth.LoginRequest
import com.financewallet.api.dto.request.auth.OAuthLoginRequest
import com.financewallet.api.dto.request.auth.RefreshTokenRequest
import com.financewallet.api.dto.request.auth.RegisterRequest
import com.financewallet.api.dto.response.auth.AuthResponse
import com.financewallet.api.dto.response.auth.UserResponse
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.service.auth.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with email and password")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.register(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "User registered successfully",
                data = authResponse
            ))
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email and password")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.login(request)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Login successful",
            data = authResponse
        ))
    }

    @PostMapping("/oauth/login")
    @Operation(summary = "OAuth Login", description = "Login or register with OAuth provider (Google/Apple)")
    fun oauthLogin(
        @Valid @RequestBody request: OAuthLoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.oauthLogin(request)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "OAuth login successful",
            data = authResponse
        ))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.refreshToken(request.refreshToken)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Token refreshed successfully",
            data = authResponse
        ))
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout and revoke refresh token")
    fun logout(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.logout(request.refreshToken)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Logout successful"
        ))
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get current authenticated user information")
    fun getCurrentUser(): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authService.getCurrentUserInfo()

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "User retrieved successfully",
            data = user
        ))
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change password for current user")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.changePassword(request)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Password changed successfully. Please login again."
        ))
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if authentication service is running")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "Finance Wallet API",
            "timestamp" to System.currentTimeMillis().toString()
        ))
    }
}