package com.financewallet.api.service.auth

import com.financewallet.api.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService {
    @Value($$"${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value($$"${jwt.access-token.expiration}")
    private var accessTokenExpiration: Long = 3600000 // 1 hour

    @Value($$"${jwt.refresh-token.expiration}")
    private var refreshTokenExpiration: Long = 2592000000 // 30 days

    /**
     * Generate JWT access token
     */
    fun generateAccessToken(user: User): String {
        val claims = HashMap<String, Any>()
        claims["userId"] = user.id.toString()
        claims["email"] = user.email
        claims["username"] = user.username
        claims["type"] = "ACCESS"

        return createToken(claims, user.email, accessTokenExpiration)
    }

    /**
     * Generate JWT refresh token
     */
    fun generateRefreshToken(user: User): String {
        val claims = HashMap<String, Any>()
        claims["userId"] = user.id.toString()
        claims["type"] = "REFRESH"

        return createToken(claims, user.email, refreshTokenExpiration)
    }

    /**
     * Create JWT token
     */
    private fun createToken(claims: Map<String, Any>, subject: String, expirationTime: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Extract username (email) from token
     */
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    /**
     * Extract user ID from token
     */
    fun extractUserId(token: String): String {
        return extractAllClaims(token)["userId"] as String
    }

    /**
     * Extract expiration date from token
     */
    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    /**
     * Extract specific claim from token
     */
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    /**
     * Extract all claims from token
     */
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    /**
     * Validate token
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username && !isTokenExpired(token))
    }

    /**
     * Validate token (without UserDetails)
     */
    fun validateToken(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get signing key for JWT
     */
    private fun getSigningKey(): Key {
        val keyBytes = Decoders.BASE64.decode(jwtSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * Get access token expiration time in milliseconds
     */
    fun getAccessTokenExpiration(): Long = accessTokenExpiration

    /**
     * Get refresh token expiration time in milliseconds
     */
    fun getRefreshTokenExpiration(): Long = refreshTokenExpiration
}