package me.silvernine.tutorial.dto

class AuthorityDto {
    var authorityName: String? = null

    constructor(authorityName: String?) {
        this.authorityName = authorityName
    }

    class AuthorityDtoBuilder internal constructor() {
        private var authorityName: String? = null
        fun authorityName(authorityName: String): AuthorityDtoBuilder {
            this.authorityName = authorityName
            return this
        }

        fun build(): AuthorityDto {
            return AuthorityDto(authorityName)
        }

        override fun toString(): String {
            return "AuthorityDto.AuthorityDtoBuilder(authorityName=" + authorityName + ")"
        }
    }

    companion object {
        fun builder(): AuthorityDtoBuilder {
            return AuthorityDtoBuilder()
        }
    }
}