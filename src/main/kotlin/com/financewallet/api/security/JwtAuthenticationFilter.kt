package com.financewallet.api.security

import com.financewallet.api.service.auth.JwtService
import com.financewallet.api.service.auth.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsServiceImpl
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null && jwtService.validateToken(jwt)) {
                val username = jwtService.extractUsername(jwt)
                val userDetails = userDetailsService.loadUserByUsername(username)

                if (jwtService.validateToken(jwt, userDetails)) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("Set authentication for user: $username")
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extract JWT token from Authorization header
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }

        return null
    }
}