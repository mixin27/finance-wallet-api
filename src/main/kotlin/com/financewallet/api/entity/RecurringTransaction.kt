package com.financewallet.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "recurring_transactions")
data class RecurringTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    val toAccount: Account? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Column(nullable = false, precision = 19, scale = 4)
    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    val currency: Currency,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val frequency: RecurringFrequency,

    @Column(nullable = false)
    val intervalValue: Int = 1,

    @Column(nullable = false)
    val startDate: LocalDate,

    var endDate: LocalDate? = null,

    @Column(nullable = false)
    var nextOccurrenceDate: LocalDate,

    var lastGeneratedDate: LocalDate? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class RecurringFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}