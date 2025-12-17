package com.financewallet.api.exception

import com.financewallet.api.dto.response.common.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String>()

        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Input validation failed",
            path = request.getDescription(false).replace("uri=", ""),
            validationErrors = errors
        )

        logger.warn("Validation error: {}", errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Resource not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Handle BadRequestException
     */
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Bad request: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(
        ex: UnauthorizedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = ex.message ?: "Authentication failed",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Unauthorized access: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Handle BadCredentialsException (wrong password)
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        ex: BadCredentialsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = "Invalid email or password",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Bad credentials: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Handle UsernameNotFoundException
     */
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(
        ex: UsernameNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = "Invalid email or password",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("User not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Handle InsufficientBalanceException
     */
    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalanceException(
        ex: InsufficientBalanceException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Insufficient Balance",
            message = ex.message ?: "Insufficient account balance",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Insufficient balance: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(
        ex: DuplicateResourceException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Duplicate Resource",
            message = ex.message ?: "Resource already exists",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.warn("Duplicate resource: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.getDescription(false).replace("uri=", "")
        )

        logger.error("Unexpected error: ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}