package me.silvernine.tutorial.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import me.silvernine.tutorial.config.JwtProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64

/** 스프링 없이 TokenProvider 만 검사합니다. 키는 테스트마다 새로 만듭니다. */
class TokenProviderTest {

    private val roles: List<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))

    private fun randomSecret(): String =
        Base64.getEncoder().encodeToString(ByteArray(64).also(SecureRandom()::nextBytes))

    private fun provider(secret: String = randomSecret(), validity: Duration = Duration.ofMinutes(30)) =
        TokenProvider(JwtProperties(secret, validity, Duration.ofDays(14)))

    @Test
    fun `created token parses back to the same user and roles`() {
        val provider = provider()

        val authentication = provider.getAuthentication(provider.createAccessToken("alice", roles))

        assertThat(authentication.name).isEqualTo("alice")
        assertThat(authentication.authorities.map { it.authority })
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
    }

    @Test
    fun `expired token throws ExpiredJwtException`() {
        val provider = provider(validity = Duration.ofSeconds(-1))
        val token = provider.createAccessToken("alice", roles)

        assertThatThrownBy { provider.getAuthentication(token) }.isInstanceOf(ExpiredJwtException::class.java)
    }

    @Test
    fun `token signed with another key is rejected`() {
        val token = provider().createAccessToken("alice", roles)

        assertThatThrownBy { provider().getAuthentication(token) }.isInstanceOf(JwtException::class.java)
    }
}
