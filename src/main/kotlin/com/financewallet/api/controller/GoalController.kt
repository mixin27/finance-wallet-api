package com.financewallet.api.controller

import com.financewallet.api.dto.request.goal.CreateGoalRequest
import com.financewallet.api.dto.request.goal.UpdateGoalProgressRequest
import com.financewallet.api.dto.request.goal.UpdateGoalRequest
import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.goal.GoalResponse
import com.financewallet.api.service.goal.GoalService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/goals")
@Tag(name = "Goals", description = "Goal management endpoints")
class GoalController(
    private val goalService: GoalService
) {
    /**
     * Get all goals
     * GET /api/goals
     * GET /api/goals?activeOnly=true
     */
    @GetMapping
    fun getAllGoals(
        @RequestParam(defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<ApiResponse<List<GoalResponse>>> {
        val goals = goalService.getAllGoals(activeOnly)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = goals,
                message = "Goals retrieved successfully"
            )
        )
    }

    /**
     * Get goal by ID
     * GET /api/goals/{id}
     */
    @GetMapping("/{id}")
    fun getGoalById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<GoalResponse>> {
        val goal = goalService.getGoalById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = goal,
                message = "Goal retrieved successfully"
            )
        )
    }

    /**
     * Create goal
     * POST /api/goals
     */
    @PostMapping
    fun createGoal(
        @Valid @RequestBody request: CreateGoalRequest
    ): ResponseEntity<ApiResponse<GoalResponse>> {
        val goal = goalService.createGoal(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = goal,
                message = "Goal created successfully"
            )
        )
    }

    /**
     * Update goal
     * PUT /api/goals/{id}
     */
    @PutMapping("/{id}")
    fun updateGoal(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateGoalRequest
    ): ResponseEntity<ApiResponse<GoalResponse>> {
        val goal = goalService.updateGoal(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = goal,
                message = "Goal updated successfully"
            )
        )
    }

    /**
     * Update goal progress
     * PATCH /api/goals/{id}/progress
     */
    @PatchMapping("/{id}/progress")
    fun updateProgress(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateGoalProgressRequest
    ): ResponseEntity<ApiResponse<GoalResponse>> {
        val goal = goalService.updateProgress(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = goal,
                message = "Goal progress updated successfully"
            )
        )
    }

    /**
     * Mark goal as completed
     * PATCH /api/goals/{id}/complete
     */
    @PatchMapping("/{id}/complete")
    fun markAsCompleted(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<GoalResponse>> {
        val goal = goalService.markAsCompleted(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = goal,
                message = "Goal marked as completed"
            )
        )
    }

    /**
     * Delete goal
     * DELETE /api/goals/{id}
     */
    @DeleteMapping("/{id}")
    fun deleteGoal(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        goalService.deleteGoal(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = Unit,
                message = "Goal deleted successfully"
            )
        )
    }
}