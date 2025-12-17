package com.financewallet.api.repository

import com.financewallet.api.entity.Account
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRepository : JpaRepository<Account, UUID> {
    fun findByUser(user: User): List<Account>
    fun findByUserAndIsActiveTrue(user: User): List<Account>
    fun findByUserAndId(user: User, id: UUID): Account?

    @Query("SELECT COALESCE(SUM(a.currentBalance), 0) FROM Account a WHERE a.user = :user AND a.isIncludedInTotal = true AND a.isActive = true")
    fun getTotalBalance(user: User): Double
}