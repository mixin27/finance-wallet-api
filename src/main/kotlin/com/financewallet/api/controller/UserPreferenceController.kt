package com.financewallet.api.controller

import com.financewallet.api.dto.request.user.UpdateUserPreferenceRequest
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.user.UserPreferenceResponse
import com.financewallet.api.service.user.UserPreferenceService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user/preferences")
class UserPreferenceController(
    private val userPreferenceService: UserPreferenceService
) {

    /**
     * Get user preferences
     * GET /api/user/preferences
     */
    @GetMapping
    fun getUserPreferences(): ResponseEntity<ApiResponse<UserPreferenceResponse>> {
        val preferences = userPreferenceService.getUserPreferences()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = preferences,
                message = "User preferences retrieved successfully"
            )
        )
    }

    /**
     * Update user preferences
     * PUT /api/user/preferences
     */
    @PutMapping
    fun updateUserPreferences(
        @Valid @RequestBody request: UpdateUserPreferenceRequest
    ): ResponseEntity<ApiResponse<UserPreferenceResponse>> {
        val preferences = userPreferenceService.updateUserPreferences(request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = preferences,
                message = "User preferences updated successfully"
            )
        )
    }

    /**
     * Reset preferences to default
     * POST /api/user/preferences/reset
     */
    @PostMapping("/reset")
    fun resetToDefault(): ResponseEntity<ApiResponse<UserPreferenceResponse>> {
        val preferences = userPreferenceService.resetToDefault()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = preferences,
                message = "User preferences reset to default successfully"
            )
        )
    }
}