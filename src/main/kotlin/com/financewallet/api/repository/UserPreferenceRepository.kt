package com.financewallet.api.repository

import com.financewallet.api.entity.UserPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreference, UUID>