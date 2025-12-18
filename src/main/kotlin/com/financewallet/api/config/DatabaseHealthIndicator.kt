package com.financewallet.api.config

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class DatabaseHealthIndicator(
    private val dataSource: DataSource
) : HealthIndicator {
    override fun health(): Health {
        return try {
            dataSource.connection.use {
                Health.up().withDetail("database", "Available").build()
            }
        } catch (e: Exception) {
            Health.down(e).withDetail("database", "Unavailable").build()
        }
    }
}