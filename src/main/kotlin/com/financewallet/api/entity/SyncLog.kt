package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "sync_log")
data class SyncLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 50)
    val entityType: String,

    @Column(nullable = false)
    val entityId: UUID,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val action: SyncAction,

    @Column(nullable = false)
    var synced: Boolean = false,

    var syncAttemptedAt: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SyncAction {
    CREATE, UPDATE, DELETE
}