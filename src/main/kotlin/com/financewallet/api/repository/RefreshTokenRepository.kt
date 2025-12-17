package com.financewallet.api.repository

import com.financewallet.api.entity.RefreshToken
import com.financewallet.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): RefreshToken?
    fun findByUser(user: User): List<RefreshToken>

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime = LocalDateTime.now())

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    fun revokeAllUserTokens(user: User)
}