package com.financewallet.api.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(name = "user_preferences")
 class UserPreference(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_currency_id")
    var defaultCurrency: Currency? = null,

    @Column(length = 10, nullable = false)
    var language: String = "en",

    @Column(length = 20, nullable = false)
    var dateFormat: String = "DD/MM/YYYY",

    @Column(nullable = false)
    var firstDayOfWeek: Short = 1,

    @Column(length = 50, nullable = false)
    var timezone: String = "UTC",

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var theme: Theme = Theme.SYSTEM,

    @Column(nullable = false)
    var enableNotifications: Boolean = true,

    @Column(nullable = false)
    var enableBiometric: Boolean = false,

    @Column(nullable = false)
    var autoBackup: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Theme {
    LIGHT, DARK, SYSTEM
}