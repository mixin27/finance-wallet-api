package com.financewallet.api.repository

import com.financewallet.api.entity.AuthProvider
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByAuthProviderAndProviderId(authProvider: AuthProvider, providerId: String): User?
}