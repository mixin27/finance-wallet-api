package com.financewallet.api.service.category

import com.financewallet.api.dto.request.category.CreateCategoryRequest
import com.financewallet.api.dto.request.category.UpdateCategoryRequest
import com.financewallet.api.dto.response.category.CategoryResponse
import com.financewallet.api.entity.Category
import com.financewallet.api.entity.CategoryType
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.CategoryRepository
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(CategoryService::class.java)

    /**
     * Initialize default system categories
     * Call this during application startup or first user registration
     */
    @Transactional
    fun initializeSystemCategories() {
        if (categoryRepository.count() > 0) {
            logger.info("System categories already initialized")
            return
        }

        logger.info("Initializing default system categories...")

        val incomeCategories = listOf(
            Category(name = "Salary", type = CategoryType.INCOME, icon = "💼", color = "#4CAF50", displayOrder = 1, isSystem = true),
            Category(name = "Freelance", type = CategoryType.INCOME, icon = "💻", color = "#2196F3", displayOrder = 2, isSystem = true),
            Category(name = "Investment", type = CategoryType.INCOME, icon = "📈", color = "#FF9800", displayOrder = 3, isSystem = true),
            Category(name = "Gift", type = CategoryType.INCOME, icon = "🎁", color = "#E91E63", displayOrder = 4, isSystem = true),
            Category(name = "Other Income", type = CategoryType.INCOME, icon = "💰", color = "#9C27B0", displayOrder = 5, isSystem = true)
        )

        val expenseCategories = listOf(
            Category(name = "Food & Dining", type = CategoryType.EXPENSE, icon = "🍽️", color = "#FF5722", displayOrder = 1, isSystem = true),
            Category(name = "Transportation", type = CategoryType.EXPENSE, icon = "🚗", color = "#3F51B5", displayOrder = 2, isSystem = true),
            Category(name = "Shopping", type = CategoryType.EXPENSE, icon = "🛍️", color = "#E91E63", displayOrder = 3, isSystem = true),
            Category(name = "Entertainment", type = CategoryType.EXPENSE, icon = "🎬", color = "#9C27B0", displayOrder = 4, isSystem = true),
            Category(name = "Bills & Utilities", type = CategoryType.EXPENSE, icon = "📄", color = "#FF9800", displayOrder = 5, isSystem = true),
            Category(name = "Healthcare", type = CategoryType.EXPENSE, icon = "🏥", color = "#F44336", displayOrder = 6, isSystem = true),
            Category(name = "Education", type = CategoryType.EXPENSE, icon = "📚", color = "#2196F3", displayOrder = 7, isSystem = true),
            Category(name = "Housing", type = CategoryType.EXPENSE, icon = "🏠", color = "#795548", displayOrder = 8, isSystem = true),
            Category(name = "Personal Care", type = CategoryType.EXPENSE, icon = "💇", color = "#00BCD4", displayOrder = 9, isSystem = true),
            Category(name = "Other Expense", type = CategoryType.EXPENSE, icon = "💸", color = "#607D8B", displayOrder = 10, isSystem = true)
        )

        categoryRepository.saveAll(incomeCategories + expenseCategories)
        logger.info("System categories initialized successfully")
    }

    /**
     * Get all available categories (system + user's custom categories)
     */
    @Transactional(readOnly = true)
    fun getAllCategories(type: CategoryType? = null): List<CategoryResponse> {
        val currentUser = authService.getCurrentUser()

        val categories = if (type != null) {
            categoryRepository.findAvailableCategories(currentUser, type)
        } else {
            categoryRepository.findAllAvailableCategories(currentUser)
        }

        return categories.map { mapToCategoryResponse(it) }
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    fun getCategoryById(categoryId: UUID): CategoryResponse {
        val currentUser = authService.getCurrentUser()

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }

        // Verify user has access to this category (system or own custom)
        if (!category.isSystem && category.user?.id != currentUser.id) {
            throw ResourceNotFoundException("Category not found with id: $categoryId")
        }

        return mapToCategoryResponse(category)
    }

    /**
     * Create custom category for user
     */
    @Transactional
    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Creating custom category '${request.name}' for user: ${currentUser.email}")

        // Validate parent category if provided
        val parentCategory = request.parentCategoryId?.let { parentId ->
            val parent = categoryRepository.findById(parentId)
                .orElseThrow { ResourceNotFoundException("Parent category not found") }

            // Verify user has access to parent
            if (!parent.isSystem && parent.user?.id != currentUser.id) {
                throw BadRequestException("Invalid parent category")
            }

            // Parent and child must have same type
            if (parent.type != request.type) {
                throw BadRequestException("Parent category type must match child category type")
            }

            parent
        }

        // Get next display order
        val userCategories = categoryRepository.findByUserAndType(currentUser, request.type)
        val maxOrder = userCategories.maxOfOrNull { it.displayOrder } ?: 0

        val category = Category(
            user = currentUser,
            parentCategory = parentCategory,
            name = request.name,
            type = request.type,
            color = request.color,
            icon = request.icon,
            displayOrder = maxOrder + 1,
            isSystem = false
        )

        val savedCategory = categoryRepository.save(category)
        logger.info("Category created successfully: ${savedCategory.id}")

        return mapToCategoryResponse(savedCategory)
    }

    /**
     * Update custom category
     */
    @Transactional
    fun updateCategory(categoryId: UUID, request: UpdateCategoryRequest): CategoryResponse {
        val currentUser = authService.getCurrentUser()

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }

        // Cannot update system categories
        if (category.isSystem) {
            throw BadRequestException("Cannot update system categories")
        }

        // Verify ownership
        if (category.user?.id != currentUser.id) {
            throw ResourceNotFoundException("Category not found with id: $categoryId")
        }

        logger.info("Updating category: ${category.id}")

        // Update fields if provided
        request.name?.let { category.name = it }
        request.color?.let { category.color = it }
        request.icon?.let { category.icon = it }
        request.displayOrder?.let { category.displayOrder = it }
        request.isActive?.let { category.isActive = it }

        category.updatedAt = LocalDateTime.now()

        val updatedCategory = categoryRepository.save(category)
        logger.info("Category updated successfully")

        return mapToCategoryResponse(updatedCategory)
    }

    /**
     * Delete custom category
     */
    @Transactional
    fun deleteCategory(categoryId: UUID) {
        val currentUser = authService.getCurrentUser()

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found with id: $categoryId") }

        // Cannot delete system categories
        if (category.isSystem) {
            throw BadRequestException("Cannot delete system categories")
        }

        // Verify ownership
        if (category.user?.id != currentUser.id) {
            throw ResourceNotFoundException("Category not found with id: $categoryId")
        }

        logger.info("Deleting category: ${category.id}")

        // Check if category has subcategories
        if (category.subCategories.isNotEmpty()) {
            throw BadRequestException("Cannot delete category with subcategories. Delete subcategories first.")
        }

        // Soft delete by marking as inactive
        category.isActive = false
        category.updatedAt = LocalDateTime.now()
        categoryRepository.save(category)

        logger.info("Category deleted (soft) successfully")
    }

    /**
     * Map Category entity to CategoryResponse DTO
     */
    private fun mapToCategoryResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id!!,
            name = category.name,
            type = category.type.name,
            color = category.color,
            icon = category.icon,
            displayOrder = category.displayOrder,
            isSystem = category.isSystem,
            isActive = category.isActive,
            parentCategoryId = category.parentCategory?.id,
            parentCategoryName = category.parentCategory?.name,
            subCategories = category.subCategories
                .filter { it.isActive }
                .map { sub ->
                    CategoryResponse(
                        id = sub.id!!,
                        name = sub.name,
                        type = sub.type.name,
                        color = sub.color,
                        icon = sub.icon,
                        displayOrder = sub.displayOrder,
                        isSystem = sub.isSystem,
                        isActive = sub.isActive,
                        parentCategoryId = category.id,
                        parentCategoryName = category.name,
                        subCategories = emptyList(),
                        createdAt = sub.createdAt,
                        updatedAt = sub.updatedAt
                    )
                }
                .sortedBy { it.displayOrder },
            createdAt = category.createdAt,
            updatedAt = category.updatedAt
        )
    }
}