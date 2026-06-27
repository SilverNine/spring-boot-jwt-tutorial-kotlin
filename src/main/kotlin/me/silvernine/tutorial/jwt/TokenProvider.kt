package me.silvernine.tutorial.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class TokenProvider(
    @param:Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.token-validity-in-seconds}") tokenValidityInSeconds: Long
) : InitializingBean {
    private val logger = LoggerFactory.getLogger(TokenProvider::class.java)
    private val tokenValidityInMilliseconds: Long = tokenValidityInSeconds * 1000
    private lateinit var key: SecretKey

    companion object {
        private const val AUTHORITIES_KEY = "auth"
    }

    override fun afterPropertiesSet() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    fun createToken(authentication: Authentication): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority ?: "" }

        val validity = Date(Date().time + tokenValidityInMilliseconds)

        return Jwts.builder()
            .subject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, Jwts.SIG.HS512)
            .expiration(validity)
            .compact()
    }

    fun getAuthentication(token: String?): Authentication {
        val claims = Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val authorities: Collection<GrantedAuthority> = claims[AUTHORITIES_KEY].toString()
            .split(",")
            .filter { it.isNotEmpty() }
            .map { SimpleGrantedAuthority(it) }

        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String?): Boolean {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            return true
        } catch (e: SecurityException) {
            logger.info("잘못된 JWT 서명입니다.")
        } catch (e: MalformedJwtException) {
            logger.info("잘못된 JWT 서명입니다.")
        } catch (e: ExpiredJwtException) {
            logger.info("만료된 JWT 토큰입니다.")
        } catch (e: UnsupportedJwtException) {
            logger.info("지원되지 않는 JWT 토큰입니다.")
        } catch (e: IllegalArgumentException) {
            logger.info("JWT 토큰이 잘못되었습니다.")
        }
        return false
    }
}