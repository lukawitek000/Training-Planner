package com.lukasz.witkowski.training.planner.backend.routing

import com.lukasz.witkowski.training.planner.backend.service.TrainingPlanService
import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingPlanRequestDto
import com.lukasz.witkowski.training.planner.dto.training.UpdateTrainingPlanRequestDto
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

fun Route.trainingPlanRoutes(trainingPlanService: TrainingPlanService) {
    authenticate("auth-jwt") {
        route(ApiRoutes.TrainingPlans.BASE) {
            get {
                val search = call.request.queryParameters["search"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val principal = call.principal<JWTPrincipal>()

                val result =
                    trainingPlanService.getTrainingPlans(
                        search = search,
                        ownerId = principal?.payload?.subject,
                        page = page,
                        limit = limit,
                    )
                call.respond(HttpStatusCode.OK, result)
            }

            get("/{id}") {
                val id =
                    call.parameters["id"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing training plan id.")
                val plan =
                    trainingPlanService.getTrainingPlanById(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, "Training plan not found.")
                call.respond(HttpStatusCode.OK, plan)
            }

            post {
                val principal =
                    call.principal<JWTPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId =
                    principal.payload.subject
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<CreateTrainingPlanRequestDto>()
                val created = trainingPlanService.createTrainingPlan(request, userId)
                call.respond(HttpStatusCode.Created, created)
            }

            put("/{id}") {
                val id =
                    call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing training plan id.")
                val principal =
                    call.principal<JWTPrincipal>()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val userId =
                    principal.payload.subject
                        ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<UpdateTrainingPlanRequestDto>()
                val updated = trainingPlanService.updateTrainingPlan(id, request, userId)
                call.respond(HttpStatusCode.OK, updated)
            }

            delete("/{id}") {
                val id =
                    call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing training plan id.")
                val principal =
                    call.principal<JWTPrincipal>()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val userId =
                    principal.payload.subject
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val success = trainingPlanService.deleteTrainingPlan(id, userId)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Training plan not found.")
                }
            }
        }
    }
}
