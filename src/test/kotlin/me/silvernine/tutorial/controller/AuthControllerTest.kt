package me.silvernine.tutorial.controller

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private data class Tokens(val access: String, val refresh: String)

    private fun signup(username: String) {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$PASSWORD","nickname":"nick"}""")
        ).andExpect(status().isOk)
    }

    private fun login(username: String, password: String = PASSWORD): Tokens {
        val body = mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$password"}""")
        ).andExpect(status().isOk)
            .andReturn().response.contentAsString
        return Tokens(JsonPath.read(body, "$.token"), JsonPath.read(body, "$.refreshToken"))
    }

    private fun refreshBody(refreshToken: String) = """{"refreshToken":"$refreshToken"}"""

    @Test
    fun `signup returns user without password`() {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"alice","password":"$PASSWORD","nickname":"nick"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.authorityDtoSet[0].authorityName").value("ROLE_USER"))
    }

    @Test
    fun `signup with invalid input returns ProblemDetail with field errors`() {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"a","password":"$PASSWORD","nickname":"nick"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
    }

    @Test
    fun `duplicate signup returns 409`() {
        signup("dup")
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"dup","password":"$PASSWORD","nickname":"nick"}""")
        ).andExpect(status().isConflict)
    }

    @Test
    fun `authenticate returns access and refresh token`() {
        signup("bob")
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"bob","password":"$PASSWORD"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andExpect(header().string("Authorization", Matchers.startsWith("Bearer ")))
    }

    @Test
    fun `wrong password returns 401`() {
        signup("mallory")
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"mallory","password":"wrong-password"}""")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    fun `my user without token returns 401 ProblemDetail`() {
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
    }

    @Test
    fun `my user with invalid token returns 401`() {
        mockMvc.perform(get("/api/user").header("Authorization", "Bearer invalid.token.value"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `my user with valid token returns 200`() {
        signup("carol")
        val tokens = login("carol")
        mockMvc.perform(get("/api/user").header("Authorization", "Bearer ${tokens.access}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("carol"))
    }

    @Test
    fun `admin endpoint with user token returns 403`() {
        signup("dave")
        val tokens = login("dave")
        mockMvc.perform(get("/api/user/dave").header("Authorization", "Bearer ${tokens.access}"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    fun `admin endpoint with admin token returns 200 and 404 for unknown user`() {
        // data.sql 의 admin 계정 (비밀번호 admin)
        val tokens = login("admin", "admin")
        mockMvc.perform(get("/api/user/user").header("Authorization", "Bearer ${tokens.access}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("user"))
        mockMvc.perform(get("/api/user/nobody").header("Authorization", "Bearer ${tokens.access}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `refresh rotates tokens and the old refresh token stops working`() {
        signup("erin")
        val first = login("erin")

        val body = mockMvc.perform(
            post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(first.refresh))
        ).andExpect(status().isOk)
            .andReturn().response.contentAsString
        val newAccess: String = JsonPath.read(body, "$.token")
        val newRefresh: String = JsonPath.read(body, "$.refreshToken")
        assertThat(newRefresh).isNotEqualTo(first.refresh)

        mockMvc.perform(get("/api/user").header("Authorization", "Bearer $newAccess"))
            .andExpect(status().isOk)

        // 한 번 쓴 Refresh Token 은 다시 쓸 수 없습니다.
        mockMvc.perform(
            post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(first.refresh))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `logout revokes the refresh token`() {
        signup("frank")
        val tokens = login("frank")

        mockMvc.perform(
            post("/api/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(tokens.refresh))
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(tokens.refresh))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `unknown refresh token returns 401`() {
        mockMvc.perform(
            post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody("not-a-real-token"))
        ).andExpect(status().isUnauthorized)
    }

    companion object {
        private const val PASSWORD = "password123"
    }
}
