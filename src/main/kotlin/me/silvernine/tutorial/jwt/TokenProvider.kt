package me.silvernine.tutorial.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import me.silvernine.tutorial.config.JwtProperties
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

/**
 * Access Token(JWT)을 만들고 검증합니다.
 *
 * 강의 영상과 달라진 점
 * - 키는 생성자에서 바로 만듭니다. InitializingBean 이 필요 없습니다.
 * - 권한은 쉼표로 이은 문자열 대신 JSON 배열로 넣습니다.
 * - 발급 시각(iat)을 넣습니다.
 * - 검증과 파싱을 한 번에 합니다. 잘못된 토큰이면 JwtException 이 납니다.
 */
@Component
class TokenProvider(properties: JwtProperties) {

    private val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret))
    val accessTokenValidity: Duration = properties.accessTokenValidity

    fun createAccessToken(username: String, authorities: Collection<GrantedAuthority>): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(username)
            .claim(AUTHORITIES_KEY, authorities.mapNotNull { it.authority })
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTokenValidity)))
            .signWith(key, Jwts.SIG.HS512)
            .compact()
    }

    /**
     * 토큰을 검증하고 인증 정보를 만듭니다.
     *
     * @throws io.jsonwebtoken.ExpiredJwtException 만료된 토큰
     * @throws io.jsonwebtoken.JwtException 서명이 틀리거나 형식이 잘못된 토큰
     * @throws IllegalArgumentException 빈 토큰
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val roles = claims[AUTHORITIES_KEY] as? List<*> ?: emptyList<Any>()
        val authorities = roles.map { SimpleGrantedAuthority(it.toString()) }

        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken.authenticated(principal, token, authorities)
    }

    companion object {
        private const val AUTHORITIES_KEY = "auth"
    }
}
