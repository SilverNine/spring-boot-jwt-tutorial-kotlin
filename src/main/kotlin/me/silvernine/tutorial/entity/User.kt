package me.silvernine.tutorial.entity

import javax.persistence.*

@Entity
@Table(name = "`user`")
class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId: Long? = null

    @Column(name = "username", length = 50, unique = true)
    var username: String? = null

    @Column(name = "password", length = 100)
    var password: String? = null

    @Column(name = "nickname", length = 50)
    var nickname: String? = null

    @Column(name = "activated")
    var isActivated = false

    @ManyToMany
    @JoinTable(
        name = "user_authority",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority_name", referencedColumnName = "authority_name")]
    )
    var authorities: Set<Authority>? = null

    constructor(
        userId: Long?, username: String?, password: String?, nickname: String?, activated: Boolean,
        authorities: Set<Authority>?
    ) {
        this.userId = userId
        this.username = username
        this.password = password
        this.nickname = nickname
        isActivated = activated
        this.authorities = authorities
    }

    constructor() {}

    class UserBuilder internal constructor() {
        private var userId: Long? = null
        private var username: String? = null
        private var password: String? = null
        private var nickname: String? = null
        private var activated = false
        private var authorities: Set<Authority>? = null
        fun userId(userId: Long?): UserBuilder {
            this.userId = userId
            return this
        }

        fun username(username: String?): UserBuilder {
            this.username = username
            return this
        }

        fun password(password: String?): UserBuilder {
            this.password = password
            return this
        }

        fun nickname(nickname: String?): UserBuilder {
            this.nickname = nickname
            return this
        }

        fun activated(activated: Boolean): UserBuilder {
            this.activated = activated
            return this
        }

        fun authorities(authorities: Set<Authority>?): UserBuilder {
            this.authorities = authorities
            return this
        }

        fun build(): User {
            return User(userId, username, password, nickname, activated, authorities)
        }

        override fun toString(): String {
            return ("User.UserBuilder(userId=" + userId + ", username=" + username + ", password=" + password
                    + ", nickname=" + nickname + ", activated=" + activated + ", authorities=" + authorities
                    + ")")
        }
    }

    companion object {
        @JvmStatic
        fun builder(): UserBuilder {
            return UserBuilder()
        }
    }
}