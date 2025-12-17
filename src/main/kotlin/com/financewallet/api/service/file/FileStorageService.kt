package com.financewallet.api.service.file

import com.financewallet.api.exception.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService(
    @Value($$"${app.upload.dir:uploads}")
    private val uploadDir: String,

    @Value($$"${app.upload.max-file-size:5242880}") // 5MB default
    private val maxFileSize: Long
) {
    private val logger = LoggerFactory.getLogger(FileStorageService::class.java)
    private val uploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    // Allowed file types for receipts/attachments
    private val allowedContentTypes = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "application/pdf",
        "image/webp"
    )

    init {
        try {
            Files.createDirectories(uploadPath)
            logger.info("Upload directory created/verified at: $uploadPath")
        } catch (e: Exception) {
            logger.error("Could not create upload directory!", e)
            throw RuntimeException("Could not create upload directory!", e)
        }
    }

    /**
     * Store file and return file path
     */
    fun storeFile(file: MultipartFile, subDirectory: String = "transactions"): FileStorageResult {
        // Validate file
        validateFile(file)

        val originalFilename = file.originalFilename ?: throw BadRequestException("File name is invalid")

        // Generate unique filename
        val fileExtension = originalFilename.substringAfterLast(".", "")
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$fileExtension"

        try {
            // Create subdirectory if needed
            val subDirPath = uploadPath.resolve(subDirectory)
            Files.createDirectories(subDirPath)

            // Store file
            val targetLocation = subDirPath.resolve(uniqueFilename)
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.info("File stored successfully: $uniqueFilename")

            return FileStorageResult(
                fileName = uniqueFilename,
                originalFileName = originalFilename,
                fileType = file.contentType ?: "application/octet-stream",
                fileSize = file.size,
                filePath = "$subDirectory/$uniqueFilename",
                fileUrl = "/uploads/$subDirectory/$uniqueFilename"
            )
        } catch (e: IOException) {
            logger.error("Failed to store file: $originalFilename", e)
            throw BadRequestException("Failed to store file: ${e.message}")
        }
    }

    /**
     * Delete file
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val fileToDelete = uploadPath.resolve(filePath).normalize()

            // Security check: ensure file is within upload directory
            if (!fileToDelete.startsWith(uploadPath)) {
                logger.warn("Attempted to delete file outside upload directory: $filePath")
                return false
            }

            val deleted = Files.deleteIfExists(fileToDelete)
            if (deleted) {
                logger.info("File deleted successfully: $filePath")
            } else {
                logger.warn("File not found for deletion: $filePath")
            }
            deleted
        } catch (e: IOException) {
            logger.error("Failed to delete file: $filePath", e)
            false
        }
    }

    /**
     * Validate uploaded file
     */
    private fun validateFile(file: MultipartFile) {
        // Check if file is empty
        if (file.isEmpty) {
            throw BadRequestException("Cannot upload empty file")
        }

        // Check file size
        if (file.size > maxFileSize) {
            throw BadRequestException(
                "File size exceeds maximum allowed size of ${maxFileSize / 1024 / 1024}MB"
            )
        }

        // Check content type
        val contentType = file.contentType
        if (contentType == null || contentType !in allowedContentTypes) {
            throw BadRequestException(
                "File type not allowed. Allowed types: JPEG, PNG, GIF, PDF, WebP"
            )
        }

        // Check filename
        val filename = file.originalFilename
        if (filename.isNullOrBlank()) {
            throw BadRequestException("Filename is invalid")
        }

        // Check for path traversal attempts
        if (filename.contains("..")) {
            throw BadRequestException("Filename contains invalid path sequence")
        }
    }

    /**
     * Get file path
     */
    fun getFilePath(fileName: String, subDirectory: String = "transactions"): Path {
        return uploadPath.resolve(subDirectory).resolve(fileName).normalize()
    }
}

/**
 * Result of file storage operation
 */
data class FileStorageResult(
    val fileName: String,
    val originalFileName: String,
    val fileType: String,
    val fileSize: Long,
    val filePath: String,
    val fileUrl: String
)