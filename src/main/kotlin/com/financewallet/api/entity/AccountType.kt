package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "account_types")
data class AccountType(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false, length = 50)
    val name: String,

    val description: String? = null,

    @Column(length = 100)
    val icon: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)