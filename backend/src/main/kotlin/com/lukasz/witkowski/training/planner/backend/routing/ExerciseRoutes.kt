package com.lukasz.witkowski.training.planner.backend.routing

import com.lukasz.witkowski.training.planner.backend.service.ExerciseService
import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.exerciseRoutes(exerciseService: ExerciseService) {
    authenticate("auth-jwt") {
        route(ApiRoutes.Exercises.BASE) {
            get("/categories") {
                val categories = exerciseService.getCategories()
                call.respond(HttpStatusCode.OK, categories)
            }

            get {
                val query = call.request.queryParameters["query"]
                val categoryId = call.request.queryParameters["category"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val principal = call.principal<JWTPrincipal>()

                val result = exerciseService.getExercises(
                    query = query,
                    categoryId = categoryId,
                    ownerId = principal?.payload?.subject,
                    page = page,
                    limit = limit
                )
                call.respond(HttpStatusCode.OK, result)
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing exercise id.")
                val exercise = exerciseService.getExerciseById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Exercise not found.")
                call.respond(HttpStatusCode.OK, exercise)
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.payload.subject
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<CreateExerciseRequestDto>()
                val created = exerciseService.createExercise(request, userId)
                call.respond(HttpStatusCode.Created, created)
            }

            put("/{id}") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing exercise id.")
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.payload.subject
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<UpdateExerciseRequestDto>()
                val updated = exerciseService.updateExercise(id, request, userId)
                call.respond(HttpStatusCode.OK, updated)
            }

            delete("/{id}") {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing exercise id.")
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.payload.subject
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val success = exerciseService.deleteExercise(id, userId)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Exercise not found.")
                }
            }
        }
    }
}
