package com.lukasz.witkowski.training.planner.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val username: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresInMs: Long,
)

@Serializable
data class AuthResponseDto(
    val user: UserDto,
    val tokens: TokenResponseDto,
)
