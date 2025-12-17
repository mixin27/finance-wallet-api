package com.financewallet.api.repository

import com.financewallet.api.entity.Account
import com.financewallet.api.entity.Category
import com.financewallet.api.entity.Transaction
import com.financewallet.api.entity.TransactionType
import com.financewallet.api.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface TransactionRepository : JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.tags WHERE t.user = :user")
    fun findByUser(user: User, pageable: Pageable): Page<Transaction>

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.tags WHERE t.user = :user AND t.account = :account")
    fun findByUserAndAccount(user: User, account: Account, pageable: Pageable): Page<Transaction>

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.tags WHERE t.user = :user AND t.category = :category")
    fun findByUserAndCategory(user: User, category: Category, pageable: Pageable): Page<Transaction>

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.tags WHERE t.user = :user AND t.type = :type")
    fun findByUserAndType(user: User, type: TransactionType, pageable: Pageable): Page<Transaction>

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.tags WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    fun findByUserAndDateRange(
        @Param("user") user: User,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    fun sumByUserAndTypeAndDateRange(
        user: User,
        type: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Double

    // For dashboard statistics
    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY t.category")
    fun getExpensesByCategory(user: User, startDate: LocalDateTime, endDate: LocalDateTime): List<Array<Any>>
}