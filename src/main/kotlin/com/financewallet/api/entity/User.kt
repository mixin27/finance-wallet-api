package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(unique = true, nullable = false, length = 100)
    val username: String,

    @Column(name = "password_hash")
    val passwordHash: String? = null,

    @Column(nullable = false)
    val fullName: String,

    @Column(length = 20)
    val phoneNumber: String? = null,

    @Column(length = 500)
    val profileImageUrl: String? = null,

    @Column(nullable = false)
    val isEmailVerified: Boolean = false,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    val authProvider: AuthProvider = AuthProvider.LOCAL,

    val providerId: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var lastLoginAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val refreshTokens: MutableList<RefreshToken> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val accounts: MutableList<Account> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val transactions: MutableList<Transaction> = mutableListOf(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var preferences: UserPreference? = null
)

enum class AuthProvider {
    LOCAL, GOOGLE, APPLE
}