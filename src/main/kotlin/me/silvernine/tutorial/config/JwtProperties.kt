package me.silvernine.tutorial.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * application.yml 의 jwt.* 설정을 타입이 있는 객체로 받습니다.
 * 강의 영상의 `@Value("\${jwt.secret}")` 방식 대신 쓰는 요즘 방식입니다.
 *
 * @property secret Base64로 인코딩한 HS512 서명 키 (64바이트 이상)
 * @property accessTokenValidity Access Token 유효 시간 (예: 30m)
 * @property refreshTokenValidity Refresh Token 유효 시간 (예: 14d)
 */
@ConfigurationProperties("jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenValidity: Duration,
    val refreshTokenValidity: Duration,
)
