package com.financewallet.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "transactions")
data class Transaction(
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
    var category: Category? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    val toAccount: Account? = null, // For transfers

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Column(nullable = false, precision = 19, scale = 4)
    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    val currency: Currency,

    @Column(precision = 20, scale = 8)
    val exchangeRate: BigDecimal? = BigDecimal.ONE,

    @Column(precision = 19, scale = 4)
    val convertedAmount: BigDecimal? = null,

    @Column(nullable = false)
    var transactionDate: LocalDateTime,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(columnDefinition = "TEXT")
    var note: String? = null,

    @Column(length = 100)
    val referenceNumber: String? = null,

    var payee: String? = null,

    var location: String? = null,

    @Column(precision = 10, scale = 8)
    var latitude: BigDecimal? = null,

    @Column(precision = 11, scale = 8)
    var longitude: BigDecimal? = null,

    @Column(nullable = false)
    val isRecurring: Boolean = false,

    val recurringTransactionId: UUID? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.COMPLETED,

    var syncedAt: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "transaction", cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachments: MutableList<TransactionAttachment> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "transaction_tags",
        joinColumns = [JoinColumn(name = "transaction_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    val tags: MutableSet<Tag> = mutableSetOf()
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class TransactionStatus {
    PENDING, COMPLETED, CANCELLED
}