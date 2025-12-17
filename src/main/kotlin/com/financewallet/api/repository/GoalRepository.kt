package com.financewallet.api.repository

import com.financewallet.api.entity.Goal
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GoalRepository : JpaRepository<Goal, UUID> {
    fun findByUser(user: User): List<Goal>
    fun findByUserAndIsCompletedFalse(user: User): List<Goal>
}