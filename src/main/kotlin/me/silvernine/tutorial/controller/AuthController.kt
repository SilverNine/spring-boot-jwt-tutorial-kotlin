package me.silvernine.tutorial.controller

import me.silvernine.tutorial.dto.LoginDto
import me.silvernine.tutorial.dto.TokenDto
import me.silvernine.tutorial.jwt.JwtFilter
import me.silvernine.tutorial.jwt.TokenProvider
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController(tokenProvider: TokenProvider, authenticationManagerBuilder: AuthenticationManagerBuilder) {
    private val tokenProvider: TokenProvider
    private val authenticationManagerBuilder: AuthenticationManagerBuilder

    init {
        this.tokenProvider = tokenProvider
        this.authenticationManagerBuilder = authenticationManagerBuilder
    }

    @PostMapping("/authenticate")
    fun authorize(@RequestBody loginDto: LoginDto): ResponseEntity<TokenDto> {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.username, loginDto.password)
        val authentication: Authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken)
        SecurityContextHolder.getContext().setAuthentication(authentication)
        val jwt: String = tokenProvider.createToken(authentication)
        val httpHeaders = HttpHeaders()
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer $jwt")
        return ResponseEntity<TokenDto>(TokenDto(jwt), httpHeaders, HttpStatus.OK)
    }
}