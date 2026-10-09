package com.lukasz.witkowski.training.planner.auth.domain.model

import kotlin.time.Instant

data class AuthTokens(
    val accessToken: AccessToken?,
    val refreshToken: String,
)

data class AccessToken(
    val token: String,
    val expiresAt: Instant,
)
