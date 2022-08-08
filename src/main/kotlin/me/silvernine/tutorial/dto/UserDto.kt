package me.silvernine.tutorial.dto

import me.silvernine.tutorial.entity.Authority
import me.silvernine.tutorial.entity.User
import java.util.stream.Collectors

class UserDto {
    var username: String? = null
    var password: String? = null
    var nickname: String? = null
    var authorityDtoSet: Set<AuthorityDto>? = null

    constructor(username: String?, password: String?, nickname: String?, authorityDtoSet: Set<AuthorityDto>?) {
        this.username = username
        this.password = password
        this.nickname = nickname
        this.authorityDtoSet = authorityDtoSet
    }

    constructor() {}

    class UserDtoBuilder internal constructor() {
        private var username: String? = null
        private var password: String? = null
        private var nickname: String? = null
        private var authorityDtoSet: Set<AuthorityDto>? = null
        fun username(username: String?): UserDtoBuilder {
            this.username = username
            return this
        }

        fun password(password: String?): UserDtoBuilder {
            this.password = password
            return this
        }

        fun nickname(nickname: String?): UserDtoBuilder {
            this.nickname = nickname
            return this
        }

        fun authorityDtoSet(authorityDtoSet: Set<AuthorityDto>?): UserDtoBuilder {
            this.authorityDtoSet = authorityDtoSet
            return this
        }

        fun build(): UserDto {
            return UserDto(username, password, nickname, authorityDtoSet)
        }

        override fun toString(): String {
            return ("UserDto.UserDtoBuilder(username=" + username + ", password=" + password + ", nickname="
                    + nickname + ", authorityDtoSet=" + authorityDtoSet + ")")
        }
    }

    companion object {
        @JvmStatic
        fun from(user: User?): UserDto? {
            return if (user == null) null else builder()
                .username(user.username)
                .nickname(user.nickname)
                .authorityDtoSet(
                    user.authorities!!.stream()
                    .map { authority: Authority ->
                        AuthorityDto.builder().authorityName(authority.authorityName!!).build()
                    }
                    .collect(Collectors.toSet()))
                .build()
        }

        fun builder(): UserDtoBuilder {
            return UserDtoBuilder()
        }
    }
}