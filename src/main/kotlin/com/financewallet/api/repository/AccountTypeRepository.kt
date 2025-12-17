package com.financewallet.api.repository

import com.financewallet.api.entity.AccountType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountTypeRepository : JpaRepository<AccountType, UUID> {
    fun findByName(name: String): AccountType?
}