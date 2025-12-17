package com.financewallet.api.config

import com.financewallet.api.service.category.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Initialize default data when application starts
 */
@Component
class DataInitializer(
    private val categoryService: CategoryService
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting data initialization...")

        try {
            // Initialize system categories
            categoryService.initializeSystemCategories()

            logger.info("Data initialization completed successfully")
        } catch (e: Exception) {
            logger.error("Error during data initialization", e)
        }
    }
}