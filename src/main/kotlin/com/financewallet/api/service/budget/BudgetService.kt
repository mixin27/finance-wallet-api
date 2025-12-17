package com.financewallet.api.service.budget

import com.financewallet.api.dto.request.budget.CreateBudgetRequest
import com.financewallet.api.dto.request.budget.UpdateBudgetRequest
import com.financewallet.api.dto.response.budget.BudgetResponse
import com.financewallet.api.dto.response.budget.BudgetProgressResponse
import com.financewallet.api.entity.Budget
import com.financewallet.api.entity.BudgetPeriod
import com.financewallet.api.entity.Category
import com.financewallet.api.entity.Transaction
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.entity.User
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.*
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.*

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val transactionRepository: TransactionRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(BudgetService::class.java)

    /**
     * Create budget
     */
    @Transactional
    fun createBudget(request: CreateBudgetRequest): BudgetResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating budget '${request.name}' for user: ${currentUser.email}")

        // Validate category if provided
        val category = request.categoryId?.let { categoryId ->
            val cat = categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }

            // Verify user has access
            if (!cat.isSystem && cat.user?.id != currentUser.id) {
                throw BadRequestException("Invalid category")
            }
            cat
        }

        // Validate currency
        val currency = currencyRepository.findById(request.currencyId)
            .orElseThrow { ResourceNotFoundException("Currency not found") }

        // Calculate end date based on period if not provided
        val endDate = request.endDate ?: calculateEndDate(request.startDate, request.period)

        // Validate dates
        if (endDate < request.startDate) {
            throw BadRequestException("End date must be after start date")
        }

        val budget = Budget(
            user = currentUser,
            category = category,
            currency = currency,
            name = request.name,
            amount = request.amount,
            period = request.period,
            startDate = request.startDate,
            endDate = endDate,
            alertThreshold = request.alertThreshold
        )

        val savedBudget = budgetRepository.save(budget)
        logger.info("Budget created successfully: ${savedBudget.id}")

        return mapToBudgetResponse(savedBudget)
    }

    /**
     * Get all budgets for current user
     */
    @Transactional(readOnly = true)
    fun getAllBudgets(activeOnly: Boolean = false): List<BudgetResponse> {
        val currentUser = authService.getCurrentUser()

        val budgets = if (activeOnly) {
            budgetRepository.findByUserAndIsActiveTrue(currentUser)
        } else {
            budgetRepository.findByUser(currentUser)
        }

        return budgets.map { mapToBudgetResponse(it) }
    }

    /**
     * Get active budgets for current period
     */
    @Transactional(readOnly = true)
    fun getActiveBudgets(): List<BudgetProgressResponse> {
        val currentUser = authService.getCurrentUser()
        val today = LocalDate.now()

        val budgets = budgetRepository.findActiveBudgetsForDate(currentUser, today)

        return budgets.map { budget ->
            val spent = calculateSpentAmount(budget)
            val remaining = budget.amount - spent
            val percentageUsed = if (budget.amount > BigDecimal.ZERO) {
                (spent.divide(budget.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            BudgetProgressResponse(
                id = budget.id!!,
                name = budget.name,
                amount = budget.amount,
                spent = spent,
                remaining = remaining,
                percentageUsed = percentageUsed,
                period = budget.period.name,
                startDate = budget.startDate,
                endDate = budget.endDate,
                alertThreshold = budget.alertThreshold,
                isOverBudget = spent > budget.amount,
                isNearLimit = percentageUsed >= budget.alertThreshold,
                categoryName = budget.category?.name,
                currencyCode = budget.currency.code,
                currencySymbol = budget.currency.symbol
            )
        }
    }

    /**
     * Get budget by ID
     */
    @Transactional(readOnly = true)
    fun getBudgetById(budgetId: UUID): BudgetResponse {
        val currentUser = authService.getCurrentUser()

        val budget = budgetRepository.findById(budgetId)
            .orElseThrow { ResourceNotFoundException("Budget not found with id: $budgetId") }

        if (budget.user.id != currentUser.id) {
            throw ResourceNotFoundException("Budget not found with id: $budgetId")
        }

        return mapToBudgetResponse(budget)
    }

    /**
     * Update budget
     */
    @Transactional
    fun updateBudget(budgetId: UUID, request: UpdateBudgetRequest): BudgetResponse {
        val currentUser = authService.getCurrentUser()

        val budget = budgetRepository.findById(budgetId)
            .orElseThrow { ResourceNotFoundException("Budget not found with id: $budgetId") }

        if (budget.user.id != currentUser.id) {
            throw ResourceNotFoundException("Budget not found with id: $budgetId")
        }

        logger.info("Updating budget: ${budget.id}")

        request.name?.let { budget.name = it }
        request.amount?.let { budget.amount = it }
        request.alertThreshold?.let { budget.alertThreshold = it }
        request.isActive?.let { budget.isActive = it }

        budget.updatedAt = LocalDateTime.now()

        val updatedBudget = budgetRepository.save(budget)
        logger.info("Budget updated successfully")

        return mapToBudgetResponse(updatedBudget)
    }

    /**
     * Delete budget
     */
    @Transactional
    fun deleteBudget(budgetId: UUID) {
        val currentUser = authService.getCurrentUser()

        val budget = budgetRepository.findById(budgetId)
            .orElseThrow { ResourceNotFoundException("Budget not found with id: $budgetId") }

        if (budget.user.id != currentUser.id) {
            throw ResourceNotFoundException("Budget not found with id: $budgetId")
        }

        logger.info("Deleting budget: ${budget.id}")

        // Soft delete
        budget.isActive = false
        budget.updatedAt = LocalDateTime.now()
        budgetRepository.save(budget)

        logger.info("Budget deleted successfully")
    }

    /**
     * Calculate spent amount for a budget
     */
    private fun calculateSpentAmount(budget: Budget): BigDecimal {
        val startDateTime = budget.startDate.atStartOfDay()
        val endDateTime = (budget.endDate ?: LocalDate.now()).atTime(LocalTime.MAX)

        val spent = if (budget.category != null) {
            // Budget for specific category
            return transactionRepository.findByUserAndCategoryAndDateRange(
                budget.user,
                budget.category!!,
                startDateTime,
                endDateTime
            ).sumOf { it.amount }
        } else {
            // Budget for all expenses
            transactionRepository.sumByUserAndTypeAndDateRange(
                budget.user,
                TransactionType.EXPENSE,
                startDateTime,
                endDateTime
            )
        }

        return BigDecimal.valueOf(spent)
    }

    /**
     * Calculate end date based on period
     */
    private fun calculateEndDate(startDate: LocalDate, period: BudgetPeriod): LocalDate {
        return when (period) {
            BudgetPeriod.DAILY -> startDate
            BudgetPeriod.WEEKLY -> startDate.plusWeeks(1).minusDays(1)
            BudgetPeriod.MONTHLY -> startDate.with(TemporalAdjusters.lastDayOfMonth())
            BudgetPeriod.YEARLY -> startDate.with(TemporalAdjusters.lastDayOfYear())
            BudgetPeriod.CUSTOM -> startDate // Must provide endDate manually
        }
    }

    /**
     * Map Budget entity to BudgetResponse DTO
     */
    private fun mapToBudgetResponse(budget: Budget): BudgetResponse {
        return BudgetResponse(
            id = budget.id!!,
            name = budget.name,
            amount = budget.amount,
            period = budget.period.name,
            startDate = budget.startDate,
            endDate = budget.endDate,
            alertThreshold = budget.alertThreshold,
            isActive = budget.isActive,
            categoryId = budget.category?.id,
            categoryName = budget.category?.name,
            currencyId = budget.currency.id!!,
            currencyCode = budget.currency.code,
            currencySymbol = budget.currency.symbol,
            createdAt = budget.createdAt,
            updatedAt = budget.updatedAt
        )
    }
}

// Extension function to find transactions by category and date range
fun TransactionRepository.findByUserAndCategoryAndDateRange(
    user: User,
    category: Category,
    startDate: LocalDateTime,
    endDate: LocalDateTime
): List<Transaction> {
    return this.findByUserAndDateRange(user, startDate, endDate, org.springframework.data.domain.Pageable.unpaged())
        .content
        .filter { it.category?.id == category.id }
}