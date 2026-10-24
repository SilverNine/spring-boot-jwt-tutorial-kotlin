package me.silvernine.tutorial.controller

import jakarta.validation.Valid
import me.silvernine.tutorial.dto.LoginDto
import me.silvernine.tutorial.dto.RefreshTokenRequest
import me.silvernine.tutorial.dto.TokenDto
import me.silvernine.tutorial.service.AuthService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController(
    private val authService: AuthService,
) {

    /** 로그인. 응답 본문과 Authorization 헤더에 Access Token 을 담습니다. */
    @PostMapping("/authenticate")
    fun authenticate(@Valid @RequestBody loginDto: LoginDto): ResponseEntity<TokenDto> =
        withAuthorizationHeader(authService.login(loginDto))

    /** Refresh Token 으로 새 Access Token 과 새 Refresh Token 을 받습니다. 보낸 Refresh Token 은 폐기됩니다. */
    @PostMapping("/token/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenDto> =
        withAuthorizationHeader(authService.refresh(requireNotNull(request.refreshToken)))

    /** 로그아웃. Refresh Token 을 폐기합니다. */
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Void> {
        authService.logout(requireNotNull(request.refreshToken))
        return ResponseEntity.noContent().build()
    }

    private fun withAuthorizationHeader(tokens: TokenDto): ResponseEntity<TokenDto> =
        ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.token}")
            .body(tokens)
}
