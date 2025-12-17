package com.financewallet.api.service.auth

import com.financewallet.api.dto.request.auth.ChangePasswordRequest
import com.financewallet.api.dto.request.auth.LoginRequest
import com.financewallet.api.dto.request.auth.OAuthLoginRequest
import com.financewallet.api.dto.request.auth.RegisterRequest
import com.financewallet.api.dto.response.auth.AuthResponse
import com.financewallet.api.dto.response.auth.UserResponse
import com.financewallet.api.entity.*
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.exception.UnauthorizedException
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.repository.RefreshTokenRepository
import com.financewallet.api.repository.UserPreferenceRepository
import com.financewallet.api.repository.UserRepository
import com.financewallet.api.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val currencyRepository: CurrencyRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Register new user with email/password
     */
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Registering new user with email: ${request.email}")

        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email is already taken")
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.username)) {
            throw BadRequestException("Username is already taken")
        }

        // Create user
        val user = User(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            authProvider = AuthProvider.LOCAL
        )

        val savedUser = userRepository.save(user)

        logger.info("User registered successfully: ${savedUser.email}")

        // Generate tokens and return response
        return generateAuthResponse(savedUser)
    }

    /**
     * Login with email/password
     */
    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        logger.info("User login attempt: ${request.email}")

        // Authenticate user
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        // Get user
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Invalid email or password")

        // Check if user is active
        if (!user.isActive) {
            throw UnauthorizedException("Account is deactivated")
        }

        // Update last login
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        logger.info("User logged in successfully: ${user.email}")

        // Generate tokens and return response
        return generateAuthResponse(user)
    }

    /**
     * OAuth login (Google/Apple)
     */
    @Transactional
    fun oauthLogin(request: OAuthLoginRequest): AuthResponse {
        logger.info("OAuth login attempt: ${request.provider} - ${request.email}")

        val authProvider = try {
            AuthProvider.valueOf(request.provider.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid OAuth provider: ${request.provider}")
        }

        // Check if user exists with this OAuth provider
        var user = userRepository.findByAuthProviderAndProviderId(authProvider, request.providerId)

        if (user == null) {
            // Check if email already exists with different provider
            if (userRepository.existsByEmail(request.email)) {
                throw BadRequestException("Email is already registered with a different login method")
            }

            // Create new user
            val username = generateUniqueUsername(request.email)

            user = User(
                email = request.email,
                username = username,
                passwordHash = null, // OAuth users don't have password
                fullName = request.fullName,
                profileImageUrl = request.profileImageUrl,
                authProvider = authProvider,
                providerId = request.providerId,
                isEmailVerified = true // OAuth providers verify email
            )

            val savedUser = userRepository.save(user)

            user = savedUser
            logger.info("New OAuth user created: ${user.email}")
        }

        // Update last login
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        logger.info("OAuth user logged in successfully: ${user.email}")

        // Generate tokens and return response
        return generateAuthResponse(user)
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    fun refreshToken(refreshTokenString: String): AuthResponse {
        logger.info("Refresh token request")

        // Find refresh token
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            ?: throw UnauthorizedException("Invalid refresh token")

        // Check if token is expired
        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw UnauthorizedException("Refresh token has expired")
        }

        // Check if token is revoked
        if (refreshToken.revoked) {
            throw UnauthorizedException("Refresh token has been revoked")
        }

        // Validate token
        if (!jwtService.validateToken(refreshTokenString)) {
            throw UnauthorizedException("Invalid refresh token")
        }

        val user = refreshToken.user

        // Check if user is active
        if (!user.isActive) {
            throw UnauthorizedException("Account is deactivated")
        }

        logger.info("Access token refreshed for user: ${user.email}")

        // Generate new tokens
        return generateAuthResponse(user)
    }

    /**
     * Change password for current user
     */
    @Transactional
    fun changePassword(request: ChangePasswordRequest) {
        val currentUser = getCurrentUser()

        // OAuth users cannot change password
        if (currentUser.authProvider != AuthProvider.LOCAL) {
            throw BadRequestException("Cannot change password for OAuth accounts")
        }

        // Verify current password
        if (currentUser.passwordHash == null ||
            !passwordEncoder.matches(request.currentPassword, currentUser.passwordHash)) {
            throw BadRequestException("Current password is incorrect")
        }

        // Update password
        currentUser.updatedAt = LocalDateTime.now()
        val updatedUser = currentUser.copy(
            passwordHash = passwordEncoder.encode(request.newPassword)
        )
        userRepository.save(updatedUser)

        // Revoke all refresh tokens (force re-login)
        refreshTokenRepository.revokeAllUserTokens(updatedUser)

        logger.info("Password changed for user: ${updatedUser.email}")
    }

    /**
     * Logout - revoke refresh token
     */
    @Transactional
    fun logout(refreshTokenString: String) {
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenString)

        if (refreshToken != null) {
            refreshToken.revoked = true
            refreshTokenRepository.save(refreshToken)
            logger.info("User logged out: ${refreshToken.user.email}")
        }
    }

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        val userPrincipal = authentication?.principal as UserPrincipal

        return userRepository.findById(userPrincipal.id)
            .orElseThrow { ResourceNotFoundException("User not found") }
    }

    /**
     * Get current user info
     */
    fun getCurrentUserInfo(): UserResponse {
        val user = getCurrentUser()
        return mapToUserResponse(user)
    }

    /**
     * Generate auth response with tokens
     */
    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        // Save refresh token to database
        val refreshTokenEntity = RefreshToken(
            user = user,
            token = refreshToken,
            expiresAt = LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtService.getAccessTokenExpiration() / 1000,
            user = mapToUserResponse(user)
        )
    }

    /**
     * Generate unique username from email
     */
    private fun generateUniqueUsername(email: String): String {
        val baseUsername = email.substringBefore("@").lowercase()
        var username = baseUsername
        var counter = 1

        while (userRepository.existsByUsername(username)) {
            username = "${baseUsername}${counter++}"
        }

        return username
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            email = user.email,
            username = user.username,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            profileImageUrl = user.profileImageUrl,
            isEmailVerified = user.isEmailVerified,
            authProvider = user.authProvider.name,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}