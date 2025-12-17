package com.financewallet.api.repository

import com.financewallet.api.entity.Category
import com.financewallet.api.entity.CategoryType
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    // System categories (available to all users)
    fun findByIsSystemTrueAndType(type: CategoryType): List<Category>

    // User's custom categories
    fun findByUserAndType(user: User, type: CategoryType): List<Category>

    // Both system and user categories
    @Query("SELECT c FROM Category c WHERE (c.isSystem = true OR c.user = :user) AND c.type = :type AND c.isActive = true ORDER BY c.displayOrder")
    fun findAvailableCategories(user: User, type: CategoryType): List<Category>

    // All categories for a user (system + custom)
    @Query("SELECT c FROM Category c WHERE (c.isSystem = true OR c.user = :user) AND c.isActive = true ORDER BY c.type, c.displayOrder")
    fun findAllAvailableCategories(user: User): List<Category>
}