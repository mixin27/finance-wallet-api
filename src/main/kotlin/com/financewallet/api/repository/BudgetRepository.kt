package com.financewallet.api.repository

import com.financewallet.api.entity.Budget
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BudgetRepository : JpaRepository<Budget, UUID> {
    fun findByUser(user: User): List<Budget>
    fun findByUserAndIsActiveTrue(user: User): List<Budget>

    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date) AND b.isActive = true")
    fun findActiveBudgetsForDate(user: User, date: LocalDate): List<Budget>
}