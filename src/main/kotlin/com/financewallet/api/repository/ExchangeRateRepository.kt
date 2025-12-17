package com.financewallet.api.repository

import com.financewallet.api.entity.Currency
import com.financewallet.api.entity.ExchangeRate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface ExchangeRateRepository : JpaRepository<ExchangeRate, UUID> {
    @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency AND er.toCurrency = :toCurrency AND er.effectiveDate <= :date ORDER BY er.effectiveDate DESC LIMIT 1")
    fun findLatestRate(fromCurrency: Currency, toCurrency: Currency, date: LocalDate = LocalDate.now()): ExchangeRate?
}