package com.financewallet.api.repository

import com.financewallet.api.entity.RecurringTransaction
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface RecurringTransactionRepository : JpaRepository<RecurringTransaction, UUID> {
    fun findByUser(user: User): List<RecurringTransaction>
    fun findByUserAndIsActiveTrue(user: User): List<RecurringTransaction>

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.isActive = true AND rt.nextOccurrenceDate <= :date")
    fun findDueRecurringTransactions(date: LocalDate): List<RecurringTransaction>
}