package me.silvernine.tutorial.dto

class TokenDto {
    var token: String? = null

    constructor(token: String?) {
        this.token = token
    }

    constructor() {}

    class TokenDtoBuilder internal constructor() {
        private var token: String? = null
        fun token(token: String?): TokenDtoBuilder {
            this.token = token
            return this
        }

        fun build(): TokenDto {
            return TokenDto(token)
        }

        override fun toString(): String {
            return "TokenDto.TokenDtoBuilder(token=" + token + ")"
        }
    }

    companion object {
        fun builder(): TokenDtoBuilder {
            return TokenDtoBuilder()
        }
    }
}