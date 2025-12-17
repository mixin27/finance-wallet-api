package com.financewallet.api.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "exchange_rates",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["from_currency_id", "to_currency_id", "effective_date"])
    ]
)
data class ExchangeRate(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    val fromCurrency: Currency,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    val toCurrency: Currency,

    @Column(nullable = false, precision = 20, scale = 8)
    val rate: BigDecimal,

    @Column(nullable = false)
    val effectiveDate: LocalDate,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)