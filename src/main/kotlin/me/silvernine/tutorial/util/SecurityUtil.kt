package me.silvernine.tutorial.util

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

object SecurityUtil {
    private val logger = LoggerFactory.getLogger(SecurityUtil::class.java)
    val currentUsername: Optional<String>
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null) {
                logger.debug("Security Context에 인증 정보가 없습니다.")
                return Optional.empty()
            }
            val username = when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                is String -> principal
                else -> null
            }
            return Optional.ofNullable(username)
        }
}
