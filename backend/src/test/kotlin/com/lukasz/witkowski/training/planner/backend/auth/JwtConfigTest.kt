package com.lukasz.witkowski.training.planner.backend.auth

import com.auth0.jwt.JWT
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import org.junit.Test

class JwtConfigTest {
    @Test
    fun `generateAccessToken generates valid JWT with expected claims`() {
        val userId = "user-123"
        val email = "test@example.com"
        val username = "TestUser"

        val token = JwtConfig.generateAccessToken(userId, email, username)
        val decoded = JwtConfig.verifier.verify(token)

        assertEquals(userId, decoded.subject)
        assertEquals(email, decoded.getClaim("email").asString())
        assertEquals(username, decoded.getClaim("username").asString())
        assertEquals(JwtConfig.ISSUER, decoded.issuer)
        assertNotNull(decoded.expiresAt)
    }

    @Test
    fun `generateRefreshToken generates valid refresh token with refresh claim`() {
        val userId = "user-123"

        val refreshToken = JwtConfig.generateRefreshToken(userId)
        val decoded = JwtConfig.verifier.verify(refreshToken)

        assertEquals(userId, decoded.subject)
        assertEquals("refresh", decoded.getClaim("type").asString())
        assertNotNull(decoded.expiresAt)
    }

    @Test
    fun `tokens compute expiration deterministically from injected Clock`() {
        val fixedNow = Instant.parse("2026-11-20T12:00:00Z")
        val fakeClock = object : Clock {
            override fun now(): Instant = fixedNow
        }

        val access = JWT.decode(
            JwtConfig.generateAccessToken("user-1", "a@b.com", "User", clock = fakeClock)
        )
        val refresh = JWT.decode(
            JwtConfig.generateRefreshToken("user-1", clock = fakeClock)
        )

        assertEquals(
            (fixedNow + JwtConfig.ACCESS_TOKEN_EXPIRATION).toJavaInstant(),
            access.expiresAtAsInstant
        )
        assertEquals(
            (fixedNow + JwtConfig.REFRESH_TOKEN_EXPIRATION).toJavaInstant(),
            refresh.expiresAtAsInstant
        )
    }
}
