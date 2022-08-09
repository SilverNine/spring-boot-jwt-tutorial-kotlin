package me.silvernine.tutorial.dto

import me.silvernine.tutorial.entity.Authority
import me.silvernine.tutorial.entity.User

data class AuthorityDto(
    var authorityName: String? = null
){
    companion object {
        fun from(authority: Authority): AuthorityDto {
            return authority.run {
                AuthorityDto(
                    authorityName = authorityName
                )
            }
        }
    }
}