package com.lukasz.witkowski.training.planner.backend.testutils

import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import java.util.UUID

val testJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

fun ApplicationTestBuilder.createJsonClient(): HttpClient =
    createClient {
        install(ContentNegotiation) {
            json(testJson)
        }
    }

data class TestUserContext(
    val client: HttpClient,
    val token: String,
    val userId: String,
    val email: String,
    val username: String,
)

suspend fun ApplicationTestBuilder.createAuthenticatedUser(
    email: String = "user_${UUID.randomUUID().toString().take(8)}@example.com",
    username: String = "user_${UUID.randomUUID().toString().take(8)}",
): TestUserContext {
    val client = createJsonClient()
    val registerRequest =
        RegisterRequestDto(
            email = email,
            username = username,
            password = "Password123!",
        )
    val response =
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }
    val authResult = response.body<AuthResponseDto>()

    return TestUserContext(
        client = client,
        token = authResult.tokens.accessToken,
        userId = authResult.user.id,
        email = authResult.user.email,
        username = authResult.user.username,
    )
}
