package com.financewallet.api.service.goal

import com.financewallet.api.dto.request.goal.CreateGoalRequest
import com.financewallet.api.dto.request.goal.UpdateGoalRequest
import com.financewallet.api.dto.request.goal.UpdateGoalProgressRequest
import com.financewallet.api.dto.response.goal.GoalResponse
import com.financewallet.api.entity.Goal
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.AccountRepository
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.repository.GoalRepository
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val accountRepository: AccountRepository,
    private val currencyRepository: CurrencyRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(GoalService::class.java)

    /**
     * Create financial goal
     */
    @Transactional
    fun createGoal(request: CreateGoalRequest): GoalResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating goal '${request.name}' for user: ${currentUser.email}")

        // Validate account if provided
        val account = request.accountId?.let { accountId ->
            accountRepository.findByUserAndId(currentUser, accountId)
                ?: throw ResourceNotFoundException("Account not found")
        }

        // Validate currency
        val currency = currencyRepository.findById(request.currencyId)
            .orElseThrow { ResourceNotFoundException("Currency not found") }

        // Validate amounts
        if (request.targetAmount <= BigDecimal.ZERO) {
            throw BadRequestException("Target amount must be greater than zero")
        }

        val initialAmount = request.initialAmount ?: BigDecimal.ZERO
        if (initialAmount < BigDecimal.ZERO) {
            throw BadRequestException("Initial amount cannot be negative")
        }

        if (initialAmount > request.targetAmount) {
            throw BadRequestException("Initial amount cannot be greater than target amount")
        }

        val goal = Goal(
            user = currentUser,
            account = account,
            currency = currency,
            name = request.name,
            description = request.description,
            targetAmount = request.targetAmount,
            currentAmount = initialAmount,
            targetDate = request.targetDate,
            color = request.color,
            icon = request.icon
        )

        // Mark as completed if initial amount equals target
        if (initialAmount >= request.targetAmount) {
            goal.isCompleted = true
        }

        val savedGoal = goalRepository.save(goal)
        logger.info("Goal created successfully: ${savedGoal.id}")

        return mapToGoalResponse(savedGoal)
    }

    /**
     * Get all goals
     */
    @Transactional(readOnly = true)
    fun getAllGoals(activeOnly: Boolean = false): List<GoalResponse> {
        val currentUser = authService.getCurrentUser()

        val goals = if (activeOnly) {
            goalRepository.findByUserAndIsCompletedFalse(currentUser)
        } else {
            goalRepository.findByUser(currentUser)
        }

        return goals.map { mapToGoalResponse(it) }
    }

    /**
     * Get goal by ID
     */
    @Transactional(readOnly = true)
    fun getGoalById(goalId: UUID): GoalResponse {
        val currentUser = authService.getCurrentUser()

        val goal = goalRepository.findById(goalId)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $goalId") }

        if (goal.user.id != currentUser.id) {
            throw ResourceNotFoundException("Goal not found with id: $goalId")
        }

        return mapToGoalResponse(goal)
    }

    /**
     * Update goal
     */
    @Transactional
    fun updateGoal(goalId: UUID, request: UpdateGoalRequest): GoalResponse {
        val currentUser = authService.getCurrentUser()

        val goal = goalRepository.findById(goalId)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $goalId") }

        if (goal.user.id != currentUser.id) {
            throw ResourceNotFoundException("Goal not found with id: $goalId")
        }

        logger.info("Updating goal: ${goal.id}")

        request.name?.let { goal.name = it }
        request.description?.let { goal.description = it }
        request.targetAmount?.let {
            if (it <= BigDecimal.ZERO) {
                throw BadRequestException("Target amount must be greater than zero")
            }
            goal.targetAmount = it
        }
        request.targetDate?.let { goal.targetDate = it }
        request.color?.let { goal.color = it }
        request.icon?.let { goal.icon = it }

        // Check if goal is now completed
        if (goal.currentAmount >= goal.targetAmount) {
            goal.isCompleted = true
        }

        goal.updatedAt = LocalDateTime.now()

        val updatedGoal = goalRepository.save(goal)
        logger.info("Goal updated successfully")

        return mapToGoalResponse(updatedGoal)
    }

    /**
     * Update goal progress (add/subtract amount)
     */
    @Transactional
    fun updateProgress(goalId: UUID, request: UpdateGoalProgressRequest): GoalResponse {
        val currentUser = authService.getCurrentUser()

        val goal = goalRepository.findById(goalId)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $goalId") }

        if (goal.user.id != currentUser.id) {
            throw ResourceNotFoundException("Goal not found with id: $goalId")
        }

        if (goal.isCompleted) {
            throw BadRequestException("Cannot update progress for completed goal")
        }

        logger.info("Updating progress for goal: ${goal.id}")

        val newAmount = goal.currentAmount + request.amount

        if (newAmount < BigDecimal.ZERO) {
            throw BadRequestException("Goal progress cannot be negative")
        }

        goal.currentAmount = newAmount

        // Check if goal is completed
        if (goal.currentAmount >= goal.targetAmount) {
            goal.isCompleted = true
            logger.info("Goal ${goal.id} marked as completed!")
        }

        goal.updatedAt = LocalDateTime.now()

        val updatedGoal = goalRepository.save(goal)
        logger.info("Goal progress updated successfully")

        return mapToGoalResponse(updatedGoal)
    }

    /**
     * Mark goal as completed
     */
    @Transactional
    fun markAsCompleted(goalId: UUID): GoalResponse {
        val currentUser = authService.getCurrentUser()

        val goal = goalRepository.findById(goalId)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $goalId") }

        if (goal.user.id != currentUser.id) {
            throw ResourceNotFoundException("Goal not found with id: $goalId")
        }

        logger.info("Marking goal as completed: ${goal.id}")

        goal.isCompleted = true
        goal.currentAmount = goal.targetAmount // Set to target
        goal.updatedAt = LocalDateTime.now()

        val updatedGoal = goalRepository.save(goal)
        logger.info("Goal marked as completed")

        return mapToGoalResponse(updatedGoal)
    }

    /**
     * Delete goal
     */
    @Transactional
    fun deleteGoal(goalId: UUID) {
        val currentUser = authService.getCurrentUser()

        val goal = goalRepository.findById(goalId)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $goalId") }

        if (goal.user.id != currentUser.id) {
            throw ResourceNotFoundException("Goal not found with id: $goalId")
        }

        logger.info("Deleting goal: ${goal.id}")
        goalRepository.delete(goal)
        logger.info("Goal deleted successfully")
    }

    /**
     * Map Goal entity to GoalResponse DTO
     */
    private fun mapToGoalResponse(goal: Goal): GoalResponse {
        val percentageComplete = if (goal.targetAmount > BigDecimal.ZERO) {
            (goal.currentAmount.divide(goal.targetAmount, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val remaining = (goal.targetAmount - goal.currentAmount).max(BigDecimal.ZERO)

        return GoalResponse(
            id = goal.id!!,
            name = goal.name,
            description = goal.description,
            targetAmount = goal.targetAmount,
            currentAmount = goal.currentAmount,
            remaining = remaining,
            percentageComplete = percentageComplete,
            targetDate = goal.targetDate,
            color = goal.color,
            icon = goal.icon,
            isCompleted = goal.isCompleted,
            accountId = goal.account?.id,
            accountName = goal.account?.name,
            currencyId = goal.currency.id!!,
            currencyCode = goal.currency.code,
            currencySymbol = goal.currency.symbol,
            createdAt = goal.createdAt,
            updatedAt = goal.updatedAt
        )
    }
}