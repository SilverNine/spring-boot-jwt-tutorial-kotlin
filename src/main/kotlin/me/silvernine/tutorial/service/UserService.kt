package me.silvernine.tutorial.service

import me.silvernine.tutorial.dto.UserDto
import me.silvernine.tutorial.dto.UserDto.Companion.from
import me.silvernine.tutorial.entity.Authority
import me.silvernine.tutorial.entity.User
import me.silvernine.tutorial.exception.DuplicateMemberException
import me.silvernine.tutorial.exception.NotFoundMemberException
import me.silvernine.tutorial.repository.UserRepository
import me.silvernine.tutorial.util.SecurityUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
    ) {
    @Transactional
    fun signup(userDto: UserDto): UserDto {
        val username = requireNotNull(userDto.username) { "username은 필수입니다." }
        val password = requireNotNull(userDto.password) { "password는 필수입니다." }
        if (userRepository.findOneWithAuthoritiesByUsername(username).isPresent) {
            throw DuplicateMemberException("이미 가입되어 있는 유저입니다.")
        }

        val user = User(
            username = username,
            password = requireNotNull(passwordEncoder.encode(password)),
            nickname = userDto.nickname,
            authorities = setOf(Authority("ROLE_USER")),
            isActivated = true
        )

        return from(userRepository.save(user))
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthorities(username: String): UserDto =
        userRepository.findOneWithAuthoritiesByUsername(username)
            .map(::from)
            .orElseThrow { NotFoundMemberException("$username -> 데이터베이스에서 찾을 수 없습니다.") }

    @Transactional(readOnly = true)
    fun getMyUserWithAuthorities(): UserDto =
        SecurityUtil.currentUsername
            .flatMap { username -> userRepository.findOneWithAuthoritiesByUsername(username) }
            .map(::from)
            .orElseThrow { NotFoundMemberException("Member not found") }
}