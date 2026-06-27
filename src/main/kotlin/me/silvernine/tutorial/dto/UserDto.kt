package me.silvernine.tutorial.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import me.silvernine.tutorial.entity.User

data class UserDto(
    @field:NotNull
    @field:Size(min = 3, max = 50)
    var username: String? = null,

    @get:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @field:NotNull
    @field:Size(min = 3, max = 100)
    var password: String? = null,

    @field:NotNull
    @field:Size(min = 3, max = 50)
    var nickname: String? = null,

    var authorityDtoSet: Set<AuthorityDto>? = null
) {
    companion object {
        fun from(user: User): UserDto = UserDto(
            username = user.username,
            nickname = user.nickname,
            authorityDtoSet = user.authorities
                .map { AuthorityDto(it.authorityName) }
                .toSet()
        )
    }
}