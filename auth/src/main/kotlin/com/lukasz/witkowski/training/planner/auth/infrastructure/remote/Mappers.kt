package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
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
    return AuthTokens(
        accessToken = tokens.accessToken,
        refreshToken = tokens.refreshToken,
        accessTokenExpiresAt = currentInstant + tokens.expiresInMs.milliseconds
    )
}