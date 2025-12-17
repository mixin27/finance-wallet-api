package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "transaction_attachments")
data class TransactionAttachment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    val transaction: Transaction,

    @Column(nullable = false)
    val fileName: String,

    @Column(length = 50)
    val fileType: String? = null,

    val fileSize: Long? = null,

    @Column(nullable = false, length = 500)
    val fileUrl: String,

    @Column(length = 500)
    val thumbnailUrl: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)