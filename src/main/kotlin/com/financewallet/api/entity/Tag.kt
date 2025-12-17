package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "tags",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name"])]
)
data class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(length = 7)
    val color: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(mappedBy = "tags")
    val transactions: MutableSet<Transaction> = mutableSetOf()
)