package com.lukasz.witkowski.training.planner.backend.training

import com.lukasz.witkowski.training.planner.backend.module
import com.lukasz.witkowski.training.planner.backend.testutils.createAuthenticatedUser
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingPlanRequestDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingPlanDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingPlanOverviewDto
import com.lukasz.witkowski.training.planner.dto.training.UpdateTrainingPlanRequestDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals

class TrainingPlanRoutesTest {
    @Test
    fun `training plan CRUD lifecycle and authorization boundaries`() =
        testApplication {
            application { module() }
            val userA = createAuthenticatedUser("planA@example.com", "PlanUserA")
            val userB = createAuthenticatedUser("planB@example.com", "PlanUserB")

            // 1. Create Exercise for Plan
            val exRes =
                userA.client
                    .post("/api/v1/exercises") {
                        header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                        contentType(ContentType.Application.Json)
                        setBody(CreateExerciseRequestDto(name = "Deadlift", description = "Back & legs"))
                    }.body<ExerciseDto>()

            // 2. Create Training Plan by User A
            val createPlanReq =
                CreateTrainingPlanRequestDto(
                    title = "Hypertrophy Upper Body",
                    description = "Mass building plan",
                    exercises =
                        listOf(
                            CreateTrainingExerciseRequestDto(
                                exerciseId = exRes.id,
                                repetitions = 8,
                                sets = 4,
                                restTimeInMillis = 120000,
                                weightInKg = 100.0,
                            ),
                        ),
                    restTimeInMillis = 180000,
                )
            val createRes =
                userA.client.post("/api/v1/training-plans") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(createPlanReq)
                }
            assertEquals(HttpStatusCode.Created, createRes.status)
            val createdPlan = createRes.body<TrainingPlanDto>()
            assertEquals("Hypertrophy Upper Body", createdPlan.title)
            assertEquals(1, createdPlan.exercises.size)
            assertEquals(userA.userId, createdPlan.ownerId)

            // 3. User B attempts to UPDATE User A's plan -> 403 Forbidden
            val updateReq =
                UpdateTrainingPlanRequestDto(
                    title = "Hacked Title",
                    description = "Hacked description",
                    exercises = emptyList(),
                )
            val forbiddenUpdateRes =
                userB.client.put("/api/v1/training-plans/${createdPlan.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userB.token}")
                    contentType(ContentType.Application.Json)
                    setBody(updateReq)
                }
            assertEquals(HttpStatusCode.Forbidden, forbiddenUpdateRes.status)

            // 4. User A updates own plan -> 200 OK
            val updateReqA =
                UpdateTrainingPlanRequestDto(
                    title = "Hypertrophy Upper Body V2",
                    description = "Updated mass building plan",
                    exercises = emptyList(),
                )
            val updateRes =
                userA.client.put("/api/v1/training-plans/${createdPlan.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(updateReqA)
                }
            assertEquals(HttpStatusCode.OK, updateRes.status)
            val updatedPlan = updateRes.body<TrainingPlanDto>()
            assertEquals("Hypertrophy Upper Body V2", updatedPlan.title)

            // 5. User B attempts to DELETE User A's plan -> 403 Forbidden
            val forbiddenDeleteRes =
                userB.client.delete("/api/v1/training-plans/${createdPlan.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userB.token}")
                }
            assertEquals(HttpStatusCode.Forbidden, forbiddenDeleteRes.status)

            // 6. User A deletes own plan -> 204 No Content
            val deleteRes =
                userA.client.delete("/api/v1/training-plans/${createdPlan.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                }
            assertEquals(HttpStatusCode.NoContent, deleteRes.status)

            // 7. Verify deletion (404 Not Found)
            val getNotFoundRes =
                userA.client.get("/api/v1/training-plans/${createdPlan.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                }
            assertEquals(HttpStatusCode.NotFound, getNotFoundRes.status)
        }

    @Test
    fun `training plans overview search filtering`() =
        testApplication {
            application { module() }
            val user = createAuthenticatedUser()

            user.client.post("/api/v1/training-plans") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(CreateTrainingPlanRequestDto(title = "Leg Day Destroy", description = "Heavy squats"))
            }
            user.client.post("/api/v1/training-plans") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(CreateTrainingPlanRequestDto(title = "Arm Blast", description = "Biceps & triceps"))
            }

            val searchRes =
                user.client.get("/api/v1/training-plans?search=leg") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.OK, searchRes.status)
            val pagedOverview = searchRes.body<PagedResponseDto<TrainingPlanOverviewDto>>()
            assertEquals(1, pagedOverview.items.size)
            assertEquals("Leg Day Destroy", pagedOverview.items.first().title)
        }
}
