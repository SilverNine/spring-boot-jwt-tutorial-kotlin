package me.silvernine.tutorial.repository

import me.silvernine.tutorial.entity.Authority
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorityRepository : JpaRepository<Authority?, String?>