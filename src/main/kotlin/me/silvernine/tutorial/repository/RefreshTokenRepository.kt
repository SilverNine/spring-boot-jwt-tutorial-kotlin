package me.silvernine.tutorial.repository

import me.silvernine.tutorial.entity.RefreshToken
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = ["user", "user.authorities"])
    fun findByTokenHash(tokenHash: String): RefreshToken?
}
