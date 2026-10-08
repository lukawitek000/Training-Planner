package com.lukasz.witkowski.training.planner.backend.auth

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun `hashPassword produces BCrypt hash that verifies correctly`() {
        val rawPassword = "SecurePassword123!"
        val hash = PasswordHasher.hashPassword(rawPassword)

        assertNotEquals(rawPassword, hash)
        assertTrue(PasswordHasher.verifyPassword(rawPassword, hash))
    }

    @Test
    fun `verifyPassword returns false for incorrect password`() {
        val rawPassword = "SecurePassword123!"
        val wrongPassword = "WrongPassword123!"
        val hash = PasswordHasher.hashPassword(rawPassword)

        assertFalse(PasswordHasher.verifyPassword(wrongPassword, hash))
    }
}
