package com.financewallet.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "goals")
data class Goal(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    val account: Account? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    val currency: Currency,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, precision = 19, scale = 4)
    val targetAmount: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 4)
    var currentAmount: BigDecimal = BigDecimal.ZERO,

    val targetDate: LocalDate? = null,

    @Column(length = 7)
    val color: String? = null,

    @Column(length = 100)
    val icon: String? = null,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)