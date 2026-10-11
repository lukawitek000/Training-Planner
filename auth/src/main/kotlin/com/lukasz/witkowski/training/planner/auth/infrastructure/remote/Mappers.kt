package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.network.AccessToken
import com.lukasz.witkowski.training.planner.network.AuthTokens
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

internal fun SignUpForm.toRegisterRequestDto(): RegisterRequestDto = RegisterRequestDto(
    email = email,
    username = username,
    password = password
)

internal fun SignInForm.toLoginRequestDto(): LoginRequestDto = LoginRequestDto(
    email = email,
    password = password
)

internal fun AuthResponseDto.toAuthTokens(currentInstant: Instant): AuthTokens =
    tokens.toAuthTokens(currentInstant)

internal fun TokenResponseDto.toAuthTokens(currentInstant: Instant): AuthTokens = AuthTokens(
    accessToken =
    AccessToken(
        token = accessToken,
        expiresAt = currentInstant + expiresInMs.milliseconds
    ),
    refreshToken = refreshToken
)

internal fun AuthTokens.toRefreshTokenRequestDto(): RefreshTokenRequestDto =
    RefreshTokenRequestDto(refreshToken = refreshToken)
