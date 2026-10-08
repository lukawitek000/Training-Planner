package com.lukasz.witkowski.training.planner.backend.exercise

import com.lukasz.witkowski.training.planner.backend.module
import com.lukasz.witkowski.training.planner.backend.testutils.createAuthenticatedUser
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
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
import kotlin.test.assertTrue

class ExerciseRoutesTest {
    @Test
    fun `get categories returns default categories`() =
        testApplication {
            application { module() }
            val user = createAuthenticatedUser()

            val response =
                user.client.get("/api/v1/exercises/categories") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val categories = response.body<List<CategoryDto>>()
            assertTrue(categories.isNotEmpty())
        }

    @Test
    fun `exercise CRUD lifecycle and authorization boundaries`() =
        testApplication {
            application { module() }
            val userA = createAuthenticatedUser("userA@example.com", "UserA")
            val userB = createAuthenticatedUser("userB@example.com", "UserB")

            // 1. Create exercise by User A
            val createReq =
                CreateExerciseRequestDto(
                    name = "Push Up",
                    description = "Upper body exercise",
                )
            val createRes =
                userA.client.post("/api/v1/exercises") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(createReq)
                }
            assertEquals(HttpStatusCode.Created, createRes.status)
            val createdEx = createRes.body<ExerciseDto>()
            assertEquals("Push Up", createdEx.name)
            assertEquals(userA.userId, createdEx.ownerId)

            // 2. Fetch exercise by ID
            val getRes =
                userA.client.get("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                }
            assertEquals(HttpStatusCode.OK, getRes.status)
            val fetchedEx = getRes.body<ExerciseDto>()
            assertEquals(createdEx.id, fetchedEx.id)

            // 3. User B attempts to UPDATE User A's exercise -> 403 Forbidden
            val updateReq =
                UpdateExerciseRequestDto(
                    name = "Push Up Modified by B",
                    description = "Hacked description",
                )
            val forbiddenUpdateRes =
                userB.client.put("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userB.token}")
                    contentType(ContentType.Application.Json)
                    setBody(updateReq)
                }
            assertEquals(HttpStatusCode.Forbidden, forbiddenUpdateRes.status)

            // 4. User A updates own exercise -> 200 OK
            val updateReqA =
                UpdateExerciseRequestDto(
                    name = "Diamond Push Up",
                    description = "Triceps focused push up",
                )
            val updateRes =
                userA.client.put("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(updateReqA)
                }
            assertEquals(HttpStatusCode.OK, updateRes.status)
            val updatedEx = updateRes.body<ExerciseDto>()
            assertEquals("Diamond Push Up", updatedEx.name)

            // 5. User B attempts to DELETE User A's exercise -> 403 Forbidden
            val forbiddenDeleteRes =
                userB.client.delete("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userB.token}")
                }
            assertEquals(HttpStatusCode.Forbidden, forbiddenDeleteRes.status)

            // 6. User A deletes own exercise -> 204 No Content
            val deleteRes =
                userA.client.delete("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                }
            assertEquals(HttpStatusCode.NoContent, deleteRes.status)

            // 7. Verify deletion (404 Not Found)
            val getNotFoundRes =
                userA.client.get("/api/v1/exercises/${createdEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                }
            assertEquals(HttpStatusCode.NotFound, getNotFoundRes.status)
        }

    @Test
    fun `exercise list search query and pagination filtering`() =
        testApplication {
            application { module() }
            val user = createAuthenticatedUser()

            // Create multiple exercises
            user.client.post("/api/v1/exercises") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(CreateExerciseRequestDto(name = "Barbell Squat", description = "Legs"))
            }
            user.client.post("/api/v1/exercises") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(CreateExerciseRequestDto(name = "Front Squat", description = "Legs"))
            }
            user.client.post("/api/v1/exercises") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(CreateExerciseRequestDto(name = "Pull Up", description = "Back"))
            }

            // Search "squat"
            val searchRes =
                user.client.get("/api/v1/exercises?query=squat") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.OK, searchRes.status)
            val pagedSearch = searchRes.body<PagedResponseDto<ExerciseDto>>()
            assertEquals(2, pagedSearch.items.size)
            assertTrue(pagedSearch.items.all { it.name.lowercase().contains("squat") })

            // Pagination page=1 limit=2
            val page1Res =
                user.client.get("/api/v1/exercises?page=1&limit=2") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.OK, page1Res.status)
            val paged1 = page1Res.body<PagedResponseDto<ExerciseDto>>()
            assertEquals(2, paged1.items.size)
            assertEquals(1, paged1.page)
            assertEquals(2, paged1.limit)
        }
}
