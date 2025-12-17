package com.financewallet.api.controller

import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.currency.CurrencyResponse
import com.financewallet.api.entity.Currency
import com.financewallet.api.exception.ResourceNotFoundException
import com.financewallet.api.repository.CurrencyRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/currencies")
@Tag(name = "Currencies", description = "Currency management endpoints")
class CurrencyController(
    private val currencyRepository: CurrencyRepository
) {
    @GetMapping
    @Operation(summary = "Get All Currencies", description = "Get list of all active currencies")
    fun getAllCurrencies(): ResponseEntity<ApiResponse<List<CurrencyResponse>>> {
        val currencies = currencyRepository.findByIsActiveTrue()
            .map { mapToCurrencyResponse(it) }

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Currencies retrieved successfully",
            data = currencies
        ))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Currency by ID", description = "Get specific currency details")
    fun getCurrencyById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<CurrencyResponse>> {
        val currency = currencyRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Currency not found with id: $id") }

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Currency retrieved successfully",
            data = mapToCurrencyResponse(currency)
        ))
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get Currency by Code", description = "Get currency by ISO code (e.g., USD, EUR)")
    fun getCurrencyByCode(
        @PathVariable code: String
    ): ResponseEntity<ApiResponse<CurrencyResponse>> {
        val currency = currencyRepository.findByCode(code.uppercase())
            ?: throw ResourceNotFoundException("Currency not found with code: $code")

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Currency retrieved successfully",
            data = mapToCurrencyResponse(currency)
        ))
    }

    private fun mapToCurrencyResponse(currency: Currency): CurrencyResponse {
        return CurrencyResponse(
            id = currency.id!!,
            code = currency.code,
            name = currency.name,
            symbol = currency.symbol,
            decimalPlaces = currency.decimalPlaces,
            isActive = currency.isActive
        )
    }
}