package me.silvernine.tutorial.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(private val tokenProvider: TokenProvider) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwt = resolveToken(request)
        val requestURI = request.requestURI
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            val authentication = tokenProvider.getAuthentication(jwt)
            SecurityContextHolder.getContext().authentication = authentication
            log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.name, requestURI)
        } else {
            log.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI)
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    companion object {
        private val log = LoggerFactory.getLogger(JwtFilter::class.java)
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}