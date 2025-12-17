package com.financewallet.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_preferences")
data class UserPreference(
    @Id
    val userId: UUID,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_currency_id")
    val defaultCurrency: Currency? = null,

    @Column(length = 10)
    val language: String = "en",

    @Column(length = 20)
    val dateFormat: String = "DD/MM/YYYY",

    val firstDayOfWeek: Short = 1,

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    val theme: Theme = Theme.LIGHT,

    val enableNotifications: Boolean = true,

    val enableBiometric: Boolean = false,

    val autoBackup: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Theme {
    LIGHT, DARK, SYSTEM
}