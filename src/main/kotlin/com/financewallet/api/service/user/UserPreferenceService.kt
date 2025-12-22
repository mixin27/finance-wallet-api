package com.financewallet.api.service.user

import com.financewallet.api.dto.request.user.UpdateUserPreferenceRequest
import com.financewallet.api.dto.response.user.UserPreferenceResponse
import com.financewallet.api.entity.Currency
import com.financewallet.api.entity.Theme
import com.financewallet.api.entity.UserPreference
import com.financewallet.api.exception.BadRequestException
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.CurrencyRepository
import com.financewallet.api.repository.UserPreferenceRepository
import com.financewallet.api.service.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class UserPreferenceService(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val currencyRepository: CurrencyRepository,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(UserPreferenceService::class.java)

    /**
     * Get user preferences (create default if not exists)
     */
    @Transactional
    fun getUserPreferences(): UserPreferenceResponse {
        val currentUser = authService.getCurrentUser()

        val preferences = userPreferenceRepository.findById(currentUser.id!!)
            .orElseGet {
                logger.info("Creating default preferences for user: ${currentUser.email}")
                createDefaultPreferences()
            }

        return mapToResponse(preferences)
    }

    /**
     * Update user preferences
     */
    @Transactional
    fun updateUserPreferences(request: UpdateUserPreferenceRequest): UserPreferenceResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Updating preferences for user: ${currentUser.email}")

        val preferences = userPreferenceRepository.findById(currentUser.id!!)
            .orElseGet { createDefaultPreferences() }

        request.defaultCurrencyId?.let { currencyId ->
            val currency = currencyRepository.findById(currencyId)
                .orElseThrow { ResourceNotFoundException("Currency not found") }
            preferences.defaultCurrency = currency
        }

        request.language?.let { preferences.language = it }
        request.dateFormat?.let { preferences.dateFormat = it }
        request.firstDayOfWeek?.let { preferences.firstDayOfWeek = it }

        request.timezone?.let { timezone ->
            // Validate timezone
            try {
                ZoneId.of(timezone)
                preferences.timezone = timezone
            } catch (e: Exception) {
                throw BadRequestException("Invalid timezone: $timezone")
            }
        }

        request.theme?.let { preferences.theme = it }
        request.enableNotifications?.let { preferences.enableNotifications = it }
        request.enableBiometric?.let { preferences.enableBiometric = it }
        request.autoBackup?.let { preferences.autoBackup = it }

        preferences.updatedAt = LocalDateTime.now()

        val updated = userPreferenceRepository.save(preferences)
        logger.info("Preferences updated successfully")

        return mapToResponse(updated)
    }

    /**
     * Reset preferences to default
     */
    @Transactional
    fun resetToDefault(): UserPreferenceResponse {
        val currentUser = authService.getCurrentUser()

        logger.info("Resetting preferences to default for user: ${currentUser.email}")

        userPreferenceRepository.deleteById(currentUser.id!!)

        val defaultPreferences = createDefaultPreferences()

        return mapToResponse(defaultPreferences)
    }

    /**
     * Create default preferences for user
     */
    private fun createDefaultPreferences(): UserPreference {
        val currentUser = authService.getCurrentUser()

        val defaultCurrency = currencyRepository.findByCode("USD")
            ?: currencyRepository.findByIsActiveTrue().firstOrNull()

        val preferences = UserPreference(
            userId = currentUser.id!!,
            defaultCurrency = defaultCurrency,
            language = "en",
            dateFormat = "DD/MM/YYYY",
            firstDayOfWeek = 1,
            timezone = "UTC", // Default timezone
            theme = Theme.SYSTEM,
            enableNotifications = true,
            enableBiometric = false,
            autoBackup = false
        )

        return userPreferenceRepository.save(preferences)
    }

    /**
     * Map to response DTO
     */
    private fun mapToResponse(preferences: UserPreference): UserPreferenceResponse {
        return UserPreferenceResponse(
            userId = preferences.userId,
            defaultCurrencyId = preferences.defaultCurrency?.id,
            defaultCurrencyCode = preferences.defaultCurrency?.code,
            defaultCurrencySymbol = preferences.defaultCurrency?.symbol,
            language = preferences.language,
            dateFormat = preferences.dateFormat,
            firstDayOfWeek = preferences.firstDayOfWeek,
            timezone = preferences.timezone,
            theme = preferences.theme.name,
            enableNotifications = preferences.enableNotifications,
            enableBiometric = preferences.enableBiometric,
            autoBackup = preferences.autoBackup,
            createdAt = preferences.createdAt,
            updatedAt = preferences.updatedAt
        )
    }

    /**
     * Get default currency for conversions
     */
    fun getDefaultCurrency(): Currency {
        val preferences = getUserPreferences()
        return currencyRepository.findById(preferences.defaultCurrencyId!!)
            .orElseThrow { BadRequestException("Default currency not found") }
    }
}