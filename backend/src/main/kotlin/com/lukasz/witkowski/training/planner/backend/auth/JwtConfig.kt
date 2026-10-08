package com.lukasz.witkowski.training.planner.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    const val ISSUER = "com.lukasz.witkowski.training.planner"
    const val AUDIENCE = "training-planner-users"
    const val REALM = "TrainingPlannerBackend"
    private const val SECRET = "training_planner_secret_jwt_key_for_development"

    val ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    val REFRESH_TOKEN_EXPIRATION_MS = 30L * 24 * 60 * 60 * 1000L // 30 days

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier =
        JWT
            .require(algorithm)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .build()

    fun generateAccessToken(
        userId: String,
        email: String,
        username: String,
    ): String =
        JWT
            .create()
            .withSubject(userId)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("email", email)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
            .sign(algorithm)

    fun generateRefreshToken(userId: String): String =
        JWT
            .create()
            .withSubject(userId)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_MS))
            .sign(algorithm)
}
