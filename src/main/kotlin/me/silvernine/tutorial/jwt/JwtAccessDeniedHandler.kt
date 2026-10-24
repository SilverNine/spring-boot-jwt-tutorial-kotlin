package me.silvernine.tutorial.jwt

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * 로그인은 했지만 권한이 없는 주소에 들어오면 403을 돌려줍니다.
 * 응답 형식은 JwtAuthenticationEntryPoint 와 같은 방식으로 ProblemDetail 로 맞춥니다.
 */
@Component
class JwtAccessDeniedHandler(
    @param:Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : AccessDeniedHandler {

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        resolver.resolveException(request, response, null, accessDeniedException)
    }
}
