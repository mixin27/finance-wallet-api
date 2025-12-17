package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "currencies")
data class Currency(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false, length = 3)
    val code: String, // ISO 4217

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 10)
    val symbol: String,

    @Column(nullable = false)
    val decimalPlaces: Short = 2,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)