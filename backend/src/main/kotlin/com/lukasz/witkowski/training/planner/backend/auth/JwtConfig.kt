package com.lukasz.witkowski.training.planner.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.io.File
import java.util.Properties
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaInstant

object JwtConfig {
    const val ISSUER = "com.lukasz.witkowski.training.planner"
    const val AUDIENCE = "training-planner-users"
    const val REALM = "TrainingPlannerBackend"

    private val secret: String by lazy { resolveSecret() }

    val ACCESS_TOKEN_EXPIRATION = 24.hours
    val REFRESH_TOKEN_EXPIRATION = 30.days
    val ACCESS_TOKEN_EXPIRATION_MS = ACCESS_TOKEN_EXPIRATION.inWholeMilliseconds
    val REFRESH_TOKEN_EXPIRATION_MS = REFRESH_TOKEN_EXPIRATION.inWholeMilliseconds

    private val algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier by lazy {
        JWT
            .require(algorithm)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .build()
    }

    private fun resolveSecret(): String {
        val envSecret = System.getenv("JWT_SECRET")
        if (!envSecret.isNullOrBlank()) return envSecret

        val candidates = listOf(File("local.properties"), File("../local.properties"))
        for (file in candidates) {
            if (file.exists()) {
                runCatching {
                    val props = Properties()
                    file.inputStream().use { props.load(it) }
                    val propSecret = props.getProperty("jwt.secret")
                    if (!propSecret.isNullOrBlank()) return propSecret
                }
            }
        }

        return "training_planner_secret_jwt_key_for_development"
    }

    fun generateAccessToken(
        userId: String,
        email: String,
        username: String,
        clock: Clock = Clock.System
    ): String = JWT
        .create()
        .withSubject(userId)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("email", email)
        .withClaim("username", username)
        .withExpiresAt((clock.now() + ACCESS_TOKEN_EXPIRATION).toJavaInstant())
        .sign(algorithm)

    fun generateRefreshToken(userId: String, clock: Clock = Clock.System): String = JWT
        .create()
        .withSubject(userId)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("type", "refresh")
        .withExpiresAt((clock.now() + REFRESH_TOKEN_EXPIRATION).toJavaInstant())
        .sign(algorithm)
}
