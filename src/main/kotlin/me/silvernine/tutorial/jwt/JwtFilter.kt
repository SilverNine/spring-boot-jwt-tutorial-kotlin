package me.silvernine.tutorial.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Authorization: Bearer 헤더의 토큰을 검증해 SecurityContext 에 인증 정보를 넣습니다.
 *
 * 토큰이 없거나 잘못되면 아무것도 넣지 않고 다음 필터로 넘깁니다. 인증이 필요한 주소라면
 * 뒤에서 JwtAuthenticationEntryPoint 가 401을 돌려줍니다.
 */
class JwtFilter(private val tokenProvider: TokenProvider) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = resolveToken(request)
        val requestURI = request.requestURI

        if (token != null) {
            try {
                val authentication = tokenProvider.getAuthentication(token)
                SecurityContextHolder.setContext(
                    SecurityContextHolder.createEmptyContext().apply { this.authentication = authentication }
                )
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.name, requestURI)
            } catch (e: ExpiredJwtException) {
                log.info("만료된 JWT 토큰입니다, uri: {}", requestURI)
            } catch (e: JwtException) {
                log.info("유효하지 않은 JWT 토큰입니다, uri: {}", requestURI)
            } catch (e: IllegalArgumentException) {
                log.info("유효하지 않은 JWT 토큰입니다, uri: {}", requestURI)
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? =
        request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.substring(BEARER_PREFIX.length)
            ?.takeIf { it.isNotBlank() }

    companion object {
        private val log = LoggerFactory.getLogger(JwtFilter::class.java)
        private const val BEARER_PREFIX = "Bearer "
    }
}
