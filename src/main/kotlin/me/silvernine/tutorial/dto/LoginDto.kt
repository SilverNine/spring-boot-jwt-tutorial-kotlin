package me.silvernine.tutorial.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class LoginDto(
    @field:NotNull
    @field:Size(min = 3, max = 50)
    var username: String? = null,

    @field:NotNull
    @field:Size(min = 3, max = 100)
    var password: String? = null
)