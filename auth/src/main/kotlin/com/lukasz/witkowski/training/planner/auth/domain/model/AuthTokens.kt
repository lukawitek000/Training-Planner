package com.lukasz.witkowski.training.planner.auth.domain.model

import kotlin.time.Instant

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: Instant
)
