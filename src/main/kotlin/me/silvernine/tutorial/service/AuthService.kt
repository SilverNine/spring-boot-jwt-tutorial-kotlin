package me.silvernine.tutorial.service

import me.silvernine.tutorial.config.JwtProperties
import me.silvernine.tutorial.dto.LoginDto
import me.silvernine.tutorial.dto.TokenDto
import me.silvernine.tutorial.entity.RefreshToken
import me.silvernine.tutorial.entity.User
import me.silvernine.tutorial.exception.InvalidRefreshTokenException
import me.silvernine.tutorial.jwt.TokenProvider
import me.silvernine.tutorial.repository.RefreshTokenRepository
import me.silvernine.tutorial.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

/**
 * 로그인, 토큰 재발급, 로그아웃.
 *
 * Access Token 은 짧게(기본 30분) 쓰고, 만료되면 Refresh Token 으로 새로 받습니다.
 * Refresh Token 은 한 번 쓰면 폐기하고 새것을 발급합니다(rotation). 탈취된 토큰을 누가 먼저 쓰면
 * 다른 쪽은 401을 받으므로 유출을 알아챌 수 있습니다.
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
) {

    @Transactional
    fun login(loginDto: LoginDto): TokenDto {
        // 아이디와 비밀번호 확인은 AuthenticationManager 가 CustomUserDetailsService 와 PasswordEncoder 로 합니다.
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(loginDto.username, loginDto.password)
        )
        val user = userRepository.findOneWithAuthoritiesByUsername(authentication.name)
            .orElseThrow { UsernameNotFoundException(authentication.name) }
        return issueTokens(user, authentication.authorities)
    }

    @Transactional(noRollbackFor = [InvalidRefreshTokenException::class])
    fun refresh(rawRefreshToken: String): TokenDto {
        val stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
            ?: throw InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다.")

        // 쓴 토큰은 결과와 상관없이 바로 폐기합니다.
        refreshTokenRepository.delete(stored)

        if (stored.isExpired(Instant.now())) {
            throw InvalidRefreshTokenException("만료된 Refresh Token입니다. 다시 로그인하세요.")
        }
        val user = stored.user
        if (!user.isActivated) {
            throw InvalidRefreshTokenException("비활성화된 계정입니다.")
        }
        return issueTokens(user, user.authorities.map { SimpleGrantedAuthority(it.authorityName) })
    }

    /** 로그아웃: Refresh Token 을 폐기합니다. Access Token 은 만료될 때까지 유효하므로 짧게 둡니다. */
    @Transactional
    fun logout(rawRefreshToken: String) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
            ?.let(refreshTokenRepository::delete)
    }

    private fun issueTokens(user: User, authorities: Collection<GrantedAuthority>): TokenDto {
        val accessToken = tokenProvider.createAccessToken(user.username, authorities)

        val refreshToken = newRefreshToken()
        val expiresAt = Instant.now().plus(jwtProperties.refreshTokenValidity)
        refreshTokenRepository.save(RefreshToken(user, hash(refreshToken), expiresAt))

        return TokenDto(accessToken, refreshToken)
    }

    companion object {
        private val RANDOM = SecureRandom()

        /** 추측할 수 없는 256비트 난수. JWT 가 아니라 DB 에서 찾는 열쇠일 뿐입니다. */
        private fun newRefreshToken(): String {
            val bytes = ByteArray(32).also(RANDOM::nextBytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }

        private fun hash(raw: String): String =
            HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.toByteArray()))
    }
}
