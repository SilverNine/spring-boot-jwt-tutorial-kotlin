package me.silvernine.tutorial.service

import me.silvernine.tutorial.repository.UserRepository
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import me.silvernine.tutorial.entity.User as UserEntity

@Component("userDetailsService")
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    @Transactional
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findOneWithAuthoritiesByUsername(username)
            .map { user -> createUser(username, user) }
            .orElseThrow { UsernameNotFoundException("$username -> 데이터베이스에서 찾을 수 없습니다.") }

    private fun createUser(username: String, user: UserEntity): User {
        if (!user.isActivated) {
            throw DisabledException("$username -> 활성화되어 있지 않습니다.")
        }

        val grantedAuthorities = user.authorities
            .map { SimpleGrantedAuthority(it.authorityName) }

        return User(user.username, user.password, grantedAuthorities)
    }
}