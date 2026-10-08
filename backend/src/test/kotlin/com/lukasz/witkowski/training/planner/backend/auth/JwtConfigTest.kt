package com.lukasz.witkowski.training.planner.backend.auth

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
