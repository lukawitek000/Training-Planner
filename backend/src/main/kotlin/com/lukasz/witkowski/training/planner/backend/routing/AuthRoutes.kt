package com.lukasz.witkowski.training.planner.backend.routing

import com.lukasz.witkowski.training.planner.backend.exception.AuthException
import com.lukasz.witkowski.training.planner.backend.service.AuthService
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route(ApiRoutes.Auth.BASE) {
        post("/register") {
            val request = call.receive<RegisterRequestDto>()
            val result = authService.register(request)
            call.respond(HttpStatusCode.Created, result)
        }

        post("/login") {
            val request = call.receive<LoginRequestDto>()
            val result = authService.login(request)
            call.respond(HttpStatusCode.OK, result)
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequestDto>()
            val result = authService.refreshToken(request.refreshToken)
            call.respond(HttpStatusCode.OK, result)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal =
                    call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val userId =
                    principal.payload.subject
                        ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user =
                    authService.getUserById(userId)
                        ?: throw AuthException.UserNotFound("User with ID $userId not found.")
                call.respond(HttpStatusCode.OK, user)
            }
        }
    }
}
