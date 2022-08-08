package me.silvernine.tutorial.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "authority")
class Authority {
    @Id
    @Column(name = "authority_name", length = 50)
    var authorityName: String? = null

    constructor(authorityName: String?) {
        this.authorityName = authorityName
    }

    constructor() {}

    class AuthorityBuilder internal constructor() {
        private var authorityName: String? = null
        fun authorityName(authorityName: String?): AuthorityBuilder {
            this.authorityName = authorityName
            return this
        }

        fun build(): Authority {
            return Authority(authorityName)
        }

        override fun toString(): String {
            return "Authority.AuthorityBuilder(authorityName=" + authorityName + ")"
        }
    }

    companion object {
        @JvmStatic
        fun builder(): AuthorityBuilder {
            return AuthorityBuilder()
        }
    }
}