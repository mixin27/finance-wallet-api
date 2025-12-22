package com.financewallet.api.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null, // null for system categories

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    val parentCategory: Category? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: CategoryType,

    @Column(length = 7)
    var color: String? = null,

    @Column(length = 100)
    var icon: String? = null,

    @Column(nullable = false)
    var displayOrder: Int = 0,

    @Column(nullable = false)
    val isSystem: Boolean = false,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @JsonIgnore
    @OneToMany(mappedBy = "parentCategory")
    val subCategories: MutableList<Category> = mutableListOf()
)

enum class CategoryType {
    INCOME, EXPENSE, TRANSFER
}