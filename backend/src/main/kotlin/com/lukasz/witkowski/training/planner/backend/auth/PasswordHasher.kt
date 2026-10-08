package com.lukasz.witkowski.training.planner.backend.auth

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {

    fun hashPassword(password: String): String =
        BCrypt.withDefaults().hashToString(12, password.toCharArray())

    fun verifyPassword(password: String, passwordHash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
}
