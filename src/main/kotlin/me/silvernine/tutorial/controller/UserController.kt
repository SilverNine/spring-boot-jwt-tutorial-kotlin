package me.silvernine.tutorial.controller

import jakarta.validation.Valid
import me.silvernine.tutorial.dto.UserDto
import me.silvernine.tutorial.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok("hello")
    }


    @PostMapping("/signup")
    fun signup(@RequestBody @Valid userDto: UserDto): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.signup(userDto))
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    fun getMyUserInfo(): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getMyUserWithAuthorities())
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun getUserInfo(@PathVariable username: String): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getUserWithAuthorities(username))
    }
}