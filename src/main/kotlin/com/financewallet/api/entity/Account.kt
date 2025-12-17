package com.financewallet.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_id", nullable = false)
    var accountType: AccountType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    var currency: Currency,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, precision = 19, scale = 4)
    val initialBalance: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 19, scale = 4)
    var currentBalance: BigDecimal = BigDecimal.ZERO,

    @Column(length = 7)
    var color: String? = null,

    @Column(length = 100)
    var icon: String? = null,

    @Column(nullable = false)
    var isIncludedInTotal: Boolean = true,

    @Column(nullable = false)
    var displayOrder: Int = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL])
    val transactions: MutableList<Transaction> = mutableListOf()
)