package me.silvernine.tutorial.dto

import me.silvernine.tutorial.entity.User
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class UserDto(
        @field:NotNull
        @field:Size(min = 3, max = 50)
        var username: String? = null,

        @field:NotNull
        @field:Size(min = 3, max = 100)
        var password: String? = null,

        @field:NotNull
        @field:Size(min = 3, max = 50)
        var nickname: String? = null,

        var authorityDtoSet: Set<AuthorityDto>? = null
) {
    companion object {
        fun from(user: User): UserDto {
            return user.run {
                UserDto(
                        username = username,
                        nickname = nickname,
                        authorityDtoSet = user.authorities!!
                                .map { authority ->
                                    AuthorityDto(authority.authorityName)
                                }
                                .toSet()
                )
            }
        }
    }
}