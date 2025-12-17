package com.financewallet.api.service.auth

import com.financewallet.api.repository.UserRepository
import com.financewallet.api.security.UserPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")

        return UserPrincipal.create(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: UUID): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User not found with id: $id") }

        return UserPrincipal.create(user)
    }
}