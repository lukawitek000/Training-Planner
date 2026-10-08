package com.lukasz.witkowski.training.planner.backend

import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingPlanRequestDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingPlanDto
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `full authentication exercise and training plan lifecycle test`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // 1. Unauthenticated request to protected endpoint returns 401
        val unauthResponse = client.get("/api/v1/exercises")
        assertEquals(HttpStatusCode.Unauthorized, unauthResponse.status)

        // 2. Register user
        val registerRequest = RegisterRequestDto(
            email = "trainer@example.com",
            username = "TrainerAlex",
            password = "SecretPassword123"
        )
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val authResult = registerResponse.body<AuthResponseDto>()
        val token = authResult.tokens.accessToken
        assertNotNull(token)

        // 3. Authenticated request to categories
        val categoriesResponse = client.get("/api/v1/exercises/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, categoriesResponse.status)
        val categories = categoriesResponse.body<List<CategoryDto>>()
        assert(categories.isNotEmpty())

        // 4. Create an exercise
        val createExerciseRequest = CreateExerciseRequestDto(
            name = "Barbell Bench Press",
            description = "Compound chest exercise",
            categoryIds = listOf(categories.first().id)
        )
        val createExerciseResponse = client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createExerciseRequest)
        }
        assertEquals(HttpStatusCode.Created, createExerciseResponse.status)
        val exercise = createExerciseResponse.body<ExerciseDto>()
        assertEquals("Barbell Bench Press", exercise.name)

        // 5. Create a training plan with created exercise
        val createPlanRequest = CreateTrainingPlanRequestDto(
            title = "Chest & Triceps Power",
            description = "High intensity strength plan",
            exercises = listOf(
                CreateTrainingExerciseRequestDto(
                    exerciseId = exercise.id,
                    repetitions = 10,
                    sets = 4,
                    restTimeInMillis = 90000,
                    weightInKg = 80.0
                )
            ),
            restTimeInMillis = 120000
        )
        val createPlanResponse = client.post("/api/v1/training-plans") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createPlanRequest)
        }
        assertEquals(HttpStatusCode.Created, createPlanResponse.status)
        val plan = createPlanResponse.body<TrainingPlanDto>()
        assertEquals("Chest & Triceps Power", plan.title)
        assertEquals(1, plan.exercises.size)
        assertEquals("Barbell Bench Press", plan.exercises.first().exercise.name)

        // 6. Get training plan by ID
        val getPlanResponse = client.get("/api/v1/training-plans/${plan.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, getPlanResponse.status)
        val fetchedPlan = getPlanResponse.body<TrainingPlanDto>()
        assertEquals(plan.id, fetchedPlan.id)
        assertEquals("Chest & Triceps Power", fetchedPlan.title)
    }
}
