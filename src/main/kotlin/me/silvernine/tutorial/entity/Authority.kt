package me.silvernine.tutorial.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "authority")
data class Authority(
    @Id
    @Column(name = "authority_name", length = 50)
    var authorityName: String? = null
)