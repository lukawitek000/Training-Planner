package com.lukasz.witkowski.training.planner.backend.auth

import com.lukasz.witkowski.training.planner.backend.module
import com.lukasz.witkowski.training.planner.backend.testutils.createJsonClient
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRoutesTest {

    @Test
    fun `registration flow succeeds with valid credentials and fails on duplicate email`() = testApplication {
        application { module() }
        val client = createJsonClient()

        val registerReq = RegisterRequestDto(
            email = "john.doe@example.com",
            username = "JohnDoe",
            password = "Password123!"
        )

        // Register success
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerReq)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val authResult = response.body<AuthResponseDto>()
        assertEquals("john.doe@example.com", authResult.user.email)
        assertEquals("JohnDoe", authResult.user.username)
        assertNotNull(authResult.tokens.accessToken)
        assertNotNull(authResult.tokens.refreshToken)

        // Register duplicate email failure
        val duplicateResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerReq)
        }
        assertEquals(HttpStatusCode.BadRequest, duplicateResponse.status)
        val error = duplicateResponse.body<ApiErrorDto>()
        assertEquals(400, error.statusCode)
    }

    @Test
    fun `login flow succeeds with correct password and fails on wrong password or unknown user`() = testApplication {
        application { module() }
        val client = createJsonClient()

        val email = "login.test@example.com"
        val password = "MySecretPassword123"
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = email, username = "LoginUser", password = password))
        }

        // Login success
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val loginResult = loginResponse.body<AuthResponseDto>()
        assertEquals(email, loginResult.user.email)

        // Login wrong password failure
        val wrongPassResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = "WrongPassword"))
        }
        assertEquals(HttpStatusCode.BadRequest, wrongPassResponse.status)

        // Login unknown email failure
        val unknownUserResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = "nobody@example.com", password = password))
        }
        assertEquals(HttpStatusCode.BadRequest, unknownUserResponse.status)
    }

    @Test
    fun `refresh token flow returns fresh access tokens for valid refresh token`() = testApplication {
        application { module() }
        val client = createJsonClient()

        val registerRes = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = "refresh@example.com", username = "RefreshUser", password = "Password123!"))
        }.body<AuthResponseDto>()

        val refreshResponse = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = registerRes.tokens.refreshToken))
        }
        assertEquals(HttpStatusCode.OK, refreshResponse.status)
        val tokens = refreshResponse.body<TokenResponseDto>()
        assertNotNull(tokens.accessToken)

        // Invalid refresh token failure
        val invalidResponse = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = "invalid-token-string"))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidResponse.status)
    }

    @Test
    fun `get me returns current profile with valid JWT and 401 without JWT`() = testApplication {
        application { module() }
        val client = createJsonClient()

        val registerRes = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = "me@example.com", username = "MeUser", password = "Password123!"))
        }.body<AuthResponseDto>()

        // 401 Unauthorized without token
        val unauthRes = client.get("/api/v1/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, unauthRes.status)

        // 200 OK with Bearer token
        val meRes = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer ${registerRes.tokens.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, meRes.status)
        val user = meRes.body<UserDto>()
        assertEquals("me@example.com", user.email)
        assertEquals("MeUser", user.username)
    }
}
