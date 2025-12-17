package com.financewallet.api.repository

import com.financewallet.api.entity.Tag
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagRepository : JpaRepository<Tag, UUID> {
    fun findByUser(user: User): List<Tag>
    fun findByUserAndName(user: User, name: String): Tag?
}