package com.financewallet.api.repository

import com.financewallet.api.entity.Currency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CurrencyRepository : JpaRepository<Currency, UUID> {
    fun findByCode(code: String): Currency?
    fun findByIsActiveTrue(): List<Currency>
}