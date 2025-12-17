package com.financewallet.api.repository

import com.financewallet.api.entity.SyncLog
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SyncLogRepository : JpaRepository<SyncLog, UUID> {
    fun findByUserAndSyncedFalse(user: User): List<SyncLog>
    fun findByUserAndEntityTypeAndEntityId(user: User, entityType: String, entityId: UUID): List<SyncLog>
}