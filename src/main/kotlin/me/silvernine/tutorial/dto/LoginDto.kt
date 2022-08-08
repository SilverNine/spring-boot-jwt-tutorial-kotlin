package me.silvernine.tutorial.dto

class LoginDto {
    var username: String? = null
    var password: String? = null

    constructor(username: String?, password: String?) {
        this.username = username
        this.password = password
    }

    constructor() {}

    class LoginDtoBuilder internal constructor() {
        private var username: String? = null
        private var password: String? = null
        fun username(username: String?): LoginDtoBuilder {
            this.username = username
            return this
        }

        fun password(password: String?): LoginDtoBuilder {
            this.password = password
            return this
        }

        fun build(): LoginDto {
            return LoginDto(username, password)
        }

        override fun toString(): String {
            return "LoginDto.LoginDtoBuilder(username=" + username + ", password=" + password + ")"
        }
    }

    companion object {
        fun builder(): LoginDtoBuilder {
            return LoginDtoBuilder()
        }
    }
}