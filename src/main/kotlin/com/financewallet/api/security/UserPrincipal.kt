package com.financewallet.api.security

import com.financewallet.api.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class UserPrincipal(
    val id: UUID,
    private val email: String,
    private val username: String,
    private val password: String?,
    val fullName: String,
    private val authorities: Collection<GrantedAuthority>,
    private val isActive: Boolean
) : UserDetails {

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                username = user.username,
                password = user.passwordHash,
                fullName = user.fullName,
                authorities = authorities,
                isActive = user.isActive
            )
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = password

    override fun getUsername(): String = email // Using email as username for login

    fun getUsernameField(): String = username // Actual username field

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = isActive

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = isActive
}