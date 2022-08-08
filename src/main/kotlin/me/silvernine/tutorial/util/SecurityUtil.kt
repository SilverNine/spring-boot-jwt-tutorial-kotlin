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
            var username: String? = null
            if (authentication.principal is UserDetails) {
                val springSecurityUser = authentication.principal as UserDetails
                username = springSecurityUser.username
            } else if (authentication.principal is String) {
                username = authentication.principal as String
            }
            return Optional.ofNullable(username)
        }
}