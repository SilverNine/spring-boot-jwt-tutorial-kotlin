package me.silvernine.tutorial.controller

import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    private fun signupBody(username: String) =
        """{"username":"$username","password":"password123","nickname":"nick"}"""

    private fun loginBody(username: String) =
        """{"username":"$username","password":"password123"}"""

    private fun signup(username: String) {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupBody(username))
        ).andExpect(status().isOk())
    }

    private fun authenticateAndGetToken(username: String): String {
        val response = mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(username))
        ).andExpect(status().isOk())
            .andReturn().response.contentAsString
        return JsonPath.read(response, "$.token")
    }

    @Test
    fun `signup returns user without password`() {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupBody("alice"))
        ).andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.authorityDtoSet[0].authorityName").value("ROLE_USER"))
    }

    @Test
    fun `signup invalid input returns 400`() {
        mockMvc.perform(
            post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"a","password":"password123","nickname":"nick"}""")
        ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `authenticate returns bearer token`() {
        signup("bob")

        mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("bob"))
        ).andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(header().string("Authorization", Matchers.startsWith("Bearer ")))
    }

    @Test
    fun `getMyUser without token returns 401`() {
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `getMyUser with invalid token returns 401`() {
        mockMvc.perform(
            get("/api/user")
                .header("Authorization", "Bearer invalid.token.value")
        ).andExpect(status().isUnauthorized())
    }

    @Test
    fun `getMyUser with valid token returns 200`() {
        signup("carol")
        val token = authenticateAndGetToken("carol")

        mockMvc.perform(
            get("/api/user")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("carol"))
    }
}
