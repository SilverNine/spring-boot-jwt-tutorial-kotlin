package me.silvernine.tutorial.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

/**
 * 발급한 Refresh Token. 원문은 저장하지 않고 SHA-256 해시만 저장합니다.
 * DB가 유출되어도 저장된 값으로는 토큰을 재발급받을 수 없습니다.
 */
@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    val tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    val id: Long? = null

    fun isExpired(now: Instant): Boolean = !expiresAt.isAfter(now)
}
