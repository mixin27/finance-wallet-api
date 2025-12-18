package com.financewallet.api.controller

import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.file.AttachmentResponse
import com.financewallet.api.entity.TransactionAttachment
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.TransactionRepository
import com.financewallet.api.service.auth.AuthService
import com.financewallet.api.service.file.FileStorageService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.util.*

@RestController
@RequestMapping("/transactions")
@Tag(name = "Attachments", description = "Attachment upload management endpoints")
class FileController(
    private val fileStorageService: FileStorageService,
    private val transactionRepository: TransactionRepository,
    private val authService: AuthService
) {
    /**
     * Upload attachment for transaction
     * POST /api/transactions/{transactionId}/attachments
     */
    @PostMapping("/{transactionId}/attachments", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    fun uploadAttachment(
        @PathVariable transactionId: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<AttachmentResponse>> {
        val currentUser = authService.getCurrentUser()

        // Verify transaction exists and belongs to user
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found") }

        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found")
        }

        // Store file
        val fileResult = fileStorageService.storeFile(file, "transactions")

        // Create attachment entity
        val attachment = TransactionAttachment(
            transaction = transaction,
            fileName = fileResult.fileName,
            fileType = fileResult.fileType,
            fileSize = fileResult.fileSize,
            fileUrl = fileResult.fileUrl,
            thumbnailUrl = null // TODO: Generate thumbnail for images
        )

        transaction.attachments.add(attachment)
        val attachResult = transactionRepository.save(transaction)

        val response = AttachmentResponse(
            id = attachResult.id!!,
            fileName = attachment.fileName,
            originalFileName = fileResult.originalFileName,
            fileType = attachment.fileType,
            fileSize = attachment.fileSize,
            fileUrl = attachment.fileUrl,
            thumbnailUrl = attachment.thumbnailUrl,
            createdAt = attachment.createdAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = response,
                message = "File uploaded successfully"
            )
        )
    }

    /**
     * Get all attachments for a transaction
     * GET /api/transactions/{transactionId}/attachments
     */
    @GetMapping("/{transactionId}/attachments")
    fun getAttachments(
        @PathVariable transactionId: UUID
    ): ResponseEntity<ApiResponse<List<AttachmentResponse>>> {
        val currentUser = authService.getCurrentUser()

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found") }

        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found")
        }

        val attachments = transaction.attachments.map { attachment ->
            AttachmentResponse(
                id = attachment.id!!,
                fileName = attachment.fileName,
                originalFileName = attachment.fileName,
                fileType = attachment.fileType,
                fileSize = attachment.fileSize,
                fileUrl = attachment.fileUrl,
                thumbnailUrl = attachment.thumbnailUrl,
                createdAt = attachment.createdAt
            )
        }

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = attachments,
                message = "Attachments retrieved successfully"
            )
        )
    }

    /**
     * Delete attachment
     * DELETE /api/transactions/{transactionId}/attachments/{attachmentId}
     */
    @DeleteMapping("/{transactionId}/attachments/{attachmentId}")
    @Transactional
    fun deleteAttachment(
        @PathVariable transactionId: UUID,
        @PathVariable attachmentId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val currentUser = authService.getCurrentUser()

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { ResourceNotFoundException("Transaction not found") }

        if (transaction.user.id != currentUser.id) {
            throw ResourceNotFoundException("Transaction not found")
        }

        val attachment = transaction.attachments.find { it.id == attachmentId }
            ?: throw ResourceNotFoundException("Attachment not found")

        // Delete physical file
        val filePath = attachment.fileUrl.removePrefix("/uploads/")
        fileStorageService.deleteFile(filePath)

        // Remove from transaction
        transaction.attachments.remove(attachment)
        transactionRepository.save(transaction)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = Unit,
                message = "Attachment deleted successfully"
            )
        )
    }

    /**
     * Download/view file
     * GET /api/uploads/{subDirectory}/{filename}
     */
    @GetMapping("/uploads/{subDirectory}/{filename:.+}")
    fun downloadFile(
        @PathVariable subDirectory: String,
        @PathVariable filename: String
    ): ResponseEntity<Resource> {
        // Load file as Resource
        val filePath = fileStorageService.getFilePath(filename, subDirectory)
        val resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw ResourceNotFoundException("File not found: $filename")
        }

        // Determine content type
        val contentType = Files.probeContentType(filePath) ?: "application/octet-stream"

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            .body(resource)
    }
}