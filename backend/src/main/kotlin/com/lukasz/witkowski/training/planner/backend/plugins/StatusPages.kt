package com.lukasz.witkowski.training.planner.backend.plugins

import com.lukasz.witkowski.training.planner.backend.exception.AuthException
import com.lukasz.witkowski.training.planner.dto.common.ApiErrorDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AuthException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiErrorDto(
                    statusCode = cause.statusCode.value,
                    message = cause.message ?: "Authentication failed.",
                    errorCode = cause.errorCode,
                ),
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorDto(statusCode = HttpStatusCode.BadRequest.value, message = cause.message ?: "Bad request"),
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorDto(
                    statusCode = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Invalid request payload",
                ),
            )
        }
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorDto(
                    statusCode = HttpStatusCode.BadRequest.value,
                    message = "Failed to parse request payload: ${cause.message}",
                ),
            )
        }
        exception<SecurityException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiErrorDto(statusCode = HttpStatusCode.Forbidden.value, message = cause.message ?: "Forbidden"),
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorDto(
                    statusCode = HttpStatusCode.InternalServerError.value,
                    message = "Internal server error: ${cause.localizedMessage}",
                ),
            )
        }
    }
}
