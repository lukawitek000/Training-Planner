package com.lukasz.witkowski.training.planner.backend.exception

import io.ktor.http.HttpStatusCode

sealed class AuthException(
    val statusCode: HttpStatusCode,
    val errorCode: String,
    message: String,
) : RuntimeException(message) {
    class UserAlreadyExists(email: String) :
        AuthException(
            statusCode = HttpStatusCode.Conflict,
            errorCode = "USER_ALREADY_EXISTS",
            message = "User with email $email already exists.",
        )

    class UserNotFound(message: String = "User not found.") :
        AuthException(
            statusCode = HttpStatusCode.NotFound,
            errorCode = "USER_NOT_FOUND",
            message = message,
        )

    class IncorrectPassword(message: String = "Incorrect password.") :
        AuthException(
            statusCode = HttpStatusCode.Unauthorized,
            errorCode = "INCORRECT_PASSWORD",
            message = message,
        )

    class InvalidRefreshToken(message: String = "Invalid or expired refresh token.") :
        AuthException(
            statusCode = HttpStatusCode.Unauthorized,
            errorCode = "INVALID_REFRESH_TOKEN",
            message = message,
        )
}
