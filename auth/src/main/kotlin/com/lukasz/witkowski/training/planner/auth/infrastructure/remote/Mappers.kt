package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

internal fun SignUpForm.toRegisterRequestDto() : RegisterRequestDto {
    return RegisterRequestDto(
        email = email,
        username = username,
        password = password
    )
}

internal fun SignInForm.toLoginRequestDto() : LoginRequestDto {
    return LoginRequestDto(
        email = email,
        password = password
    )
}

internal fun AuthResponseDto.toAuthTokens(currentInstant: Instant): AuthTokens {
    return tokens.toAuthTokens(currentInstant)
}

internal fun TokenResponseDto.toAuthTokens(currentInstant: Instant) : AuthTokens {
    return AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpiresAt = currentInstant + expiresInMs.milliseconds
    )
}

internal fun AuthTokens.toRefreshTokenRequestDto(): RefreshTokenRequestDto {
    return RefreshTokenRequestDto(refreshToken = refreshToken)
}