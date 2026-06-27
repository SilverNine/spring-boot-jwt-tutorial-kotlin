package me.silvernine.tutorial.service

import me.silvernine.tutorial.dto.UserDto
import me.silvernine.tutorial.exception.DuplicateMemberException
import me.silvernine.tutorial.exception.NotFoundMemberException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    lateinit var userService: UserService

    private fun newUser(username: String) = UserDto(
        username = username,
        password = "password123",
        nickname = username
    )

    @Test
    fun `signup persists user with ROLE_USER`() {
        val saved = userService.signup(newUser("dave"))

        assertThat(saved.username).isEqualTo("dave")
        assertThat(saved.password).isNull()
        assertThat(saved.authorityDtoSet)
            .extracting("authorityName")
            .containsExactly("ROLE_USER")
    }

    @Test
    fun `signup duplicate throws DuplicateMemberException`() {
        userService.signup(newUser("erin"))

        assertThatThrownBy { userService.signup(newUser("erin")) }
            .isInstanceOf(DuplicateMemberException::class.java)
    }

    @Test
    fun `getUserWithAuthorities not found throws NotFoundMemberException`() {
        assertThatThrownBy { userService.getUserWithAuthorities("no-such-user") }
            .isInstanceOf(NotFoundMemberException::class.java)
    }
}
