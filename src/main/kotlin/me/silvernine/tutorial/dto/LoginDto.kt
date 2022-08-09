package me.silvernine.tutorial.dto

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class LoginDto(
    @field:NotNull
    @field:Size(min = 3, max = 50)
    var username: String? = null,

    @field:NotNull
    @field:Size(min = 3, max = 100)
    var password: String? = null
)