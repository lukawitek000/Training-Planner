package com.lukasz.witkowski.training.planner.backend.exercise

import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.ExercisesTable
import com.lukasz.witkowski.training.planner.backend.module
import com.lukasz.witkowski.training.planner.backend.testutils.createAuthenticatedUser
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseRecommendationDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
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
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.exposed.v1.jdbc.insert
import org.junit.Test

class ExerciseRoutesTest {
    @Test
    fun `get categories returns default categories`() = testApplication {
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
    fun `exercise CRUD lifecycle and authorization boundaries`() = testApplication {
        application { module() }
        val userA = createAuthenticatedUser("userA@example.com", "UserA")
        val userB = createAuthenticatedUser("userB@example.com", "UserB")

        // 1. Create exercise by User A
        val createReq =
            CreateExerciseRequestDto(
                name = "Push Up",
                description = "Upper body exercise",
                categoryIds = emptyList(),
                sharing = "PRIVATE",
                recommendations = null
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
        assertEquals(userA.userId, createdEx.owner?.id)
        assertEquals("UserA", createdEx.owner?.username)

        // 2. Fetch exercise by ID by User A -> 200 OK
        val getRes =
            userA.client.get("/api/v1/exercises/${createdEx.id}") {
                header(HttpHeaders.Authorization, "Bearer ${userA.token}")
            }
        assertEquals(HttpStatusCode.OK, getRes.status)
        val fetchedEx = getRes.body<ExerciseDto>()
        assertEquals(createdEx.id, fetchedEx.id)

        // 2b. User B attempts to GET User A's private exercise -> 403 Forbidden
        val forbiddenGetRes =
            userB.client.get("/api/v1/exercises/${createdEx.id}") {
                header(HttpHeaders.Authorization, "Bearer ${userB.token}")
            }
        assertEquals(HttpStatusCode.Forbidden, forbiddenGetRes.status)

        // 3. User B attempts to UPDATE User A's exercise -> 403 Forbidden
        val updateReq =
            UpdateExerciseRequestDto(
                name = "Push Up Modified by B",
                description = "Hacked description",
                categoryIds = emptyList(),
                sharing = "PRIVATE",
                expectedVersion = null,
                idempotencyKey = null,
                recommendations = null
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
                categoryIds = emptyList(),
                sharing = "PRIVATE",
                expectedVersion = null,
                idempotencyKey = null,
                recommendations = null
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
    fun `exercise list search query and pagination filtering`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser()

        // Create multiple exercises
        user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(
                CreateExerciseRequestDto(
                    name = "Barbell Squat",
                    description = "Legs",
                    categoryIds = emptyList(),
                    sharing = "PRIVATE",
                    recommendations = null
                )
            )
        }
        user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(
                CreateExerciseRequestDto(
                    name = "Front Squat",
                    description = "Legs",
                    categoryIds = emptyList(),
                    sharing = "PRIVATE",
                    recommendations = null
                )
            )
        }
        user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(
                CreateExerciseRequestDto(
                    name = "Pull Up",
                    description = "Back",
                    categoryIds = emptyList(),
                    sharing = "PRIVATE",
                    recommendations = null
                )
            )
        }

        // Search "squat"
        val searchRes =
            user.client.get("/api/v1/exercises?query=squat") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
            }
        assertEquals(HttpStatusCode.OK, searchRes.status)
        val pagedSearch = searchRes.body<PagedResponseDto<ExerciseDto>>()
        assertTrue(pagedSearch.items.size >= 2)
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

    @Test
    fun `malformed JSON payload returns 400 Bad Request`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser()

        val badJsonRes =
            user.client.post("/api/v1/exercises") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody("{ invalid_json: ")
            }
        assertEquals(HttpStatusCode.BadRequest, badJsonRes.status)
    }

    @Test
    fun `get exercise changes returns incremental changes and deletions`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser()

        val initialChangesRes = user.client.get("/api/v1/exercises/changes?sinceVersion=0") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
        }
        assertEquals(HttpStatusCode.OK, initialChangesRes.status)
        val initialChanges = initialChangesRes.body<ExerciseSyncResponseDto>()
        val startVersion = initialChanges.currentVersion

        val createRes = user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(
                CreateExerciseRequestDto(
                    name = "Sync Bench Press",
                    description = "Chest",
                    categoryIds = emptyList(),
                    sharing = "COMMUNITY",
                    recommendations = null
                )
            )
        }
        assertEquals(HttpStatusCode.Created, createRes.status)
        val created = createRes.body<ExerciseDto>()

        val newChangesRes = user.client.get("/api/v1/exercises/changes?sinceVersion=$startVersion") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
        }
        assertEquals(HttpStatusCode.OK, newChangesRes.status)
        val newChanges = newChangesRes.body<ExerciseSyncResponseDto>()
        assertEquals(1, newChanges.changes.size)
        assertEquals("Sync Bench Press", newChanges.changes.first().name)
        assertEquals("COMMUNITY", newChanges.changes.first().sharing)
        assertEquals(user.userId, newChanges.changes.first().owner?.id)
        assertTrue(newChanges.currentVersion > startVersion)

        val delVersion = newChanges.currentVersion
        user.client.delete("/api/v1/exercises/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
        }

        val deleteChangesRes = user.client.get("/api/v1/exercises/changes?sinceVersion=$delVersion") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
        }
        assertEquals(HttpStatusCode.OK, deleteChangesRes.status)
        val deleteChanges = deleteChangesRes.body<ExerciseSyncResponseDto>()
        assertTrue(created.id in deleteChanges.deletedIds)
    }

    @Test
    fun `create update and filter exercises using category names`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser()

        val createRes =
            user.client.post("/api/v1/exercises") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(
                    CreateExerciseRequestDto(
                        name = "Incline Dumbbell Press",
                        description = "Upper chest exercise",
                        categoryIds = listOf("Chest", "Shoulders", "chest"),
                        sharing = "PRIVATE",
                        recommendations = null
                    )
                )
            }
        assertEquals(HttpStatusCode.Created, createRes.status)
        val created = createRes.body<ExerciseDto>()
        assertEquals(2, created.categories.size)
        assertTrue(created.categories.any { it.name == "Chest" })
        assertTrue(created.categories.any { it.name == "Shoulders" })

        // Filter by category name "Chest"
        val filterByNameRes =
            user.client.get("/api/v1/exercises?category=Chest") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
            }
        assertEquals(HttpStatusCode.OK, filterByNameRes.status)
        val filteredByName = filterByNameRes.body<PagedResponseDto<ExerciseDto>>()
        assertTrue(filteredByName.items.any { it.id == created.id })

        // Update using a category name
        val updateRes =
            user.client.put("/api/v1/exercises/${created.id}") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateExerciseRequestDto(
                        name = "Incline Dumbbell Press Updated",
                        description = "Shoulders & Arms",
                        categoryIds = listOf("Shoulders", "Arms"),
                        sharing = "PRIVATE",
                        expectedVersion = created.revision,
                        idempotencyKey = null,
                        recommendations = null
                    )
                )
            }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updated = updateRes.body<ExerciseDto>()
        assertEquals(2, updated.categories.size)
        assertTrue(updated.categories.any { it.name == "Shoulders" })
        assertTrue(updated.categories.any { it.name == "Arms" })
    }

    @Test
    fun `bundled exercises are immutable and return 403 Forbidden on PUT and DELETE`() =
        testApplication {
            application { module() }
            val user = createAuthenticatedUser()
            val bundledId = UUID.randomUUID().toString()

            dbQuery {
                ExercisesTable.insert {
                    it[id] = bundledId
                    it[name] = "Bundled Burpee"
                    it[description] = "Standard bundled exercise"
                    it[ownerId] = null
                    it[sourceType] = "BUNDLED"
                    it[sharing] = "PUBLIC"
                    it[revision] = 1L
                    it[changeVersion] = 1L
                }
            }

            val getRes =
                user.client.get("/api/v1/exercises/$bundledId") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.OK, getRes.status)

            val putRes =
                user.client.put("/api/v1/exercises/$bundledId") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                    contentType(ContentType.Application.Json)
                    setBody(
                        UpdateExerciseRequestDto(
                            name = "Modified Bundled Burpee",
                            description = "Should fail",
                            categoryIds = emptyList(),
                            sharing = "PUBLIC",
                            expectedVersion = 1L,
                            idempotencyKey = null,
                            recommendations = null
                        )
                    )
                }
            assertEquals(HttpStatusCode.Forbidden, putRes.status)

            val deleteRes =
                user.client.delete("/api/v1/exercises/$bundledId") {
                    header(HttpHeaders.Authorization, "Bearer ${user.token}")
                }
            assertEquals(HttpStatusCode.Forbidden, deleteRes.status)
        }

    @Test
    fun `community exercise is visible to other users in list and by ID while private is not`() =
        testApplication {
            application { module() }
            val owner = createAuthenticatedUser("commOwner@example.com", "CommOwner")
            val viewer = createAuthenticatedUser("commViewer@example.com", "CommViewer")

            val communityEx =
                owner.client
                    .post("/api/v1/exercises") {
                        header(HttpHeaders.Authorization, "Bearer ${owner.token}")
                        contentType(ContentType.Application.Json)
                        setBody(
                            CreateExerciseRequestDto(
                                name = "Community Kettlebell Swing",
                                description = "Shared with community",
                                categoryIds = listOf("Full Body"),
                                sharing = "COMMUNITY",
                                recommendations = null
                            )
                        )
                    }.body<ExerciseDto>()

            val privateEx =
                owner.client
                    .post("/api/v1/exercises") {
                        header(HttpHeaders.Authorization, "Bearer ${owner.token}")
                        contentType(ContentType.Application.Json)
                        setBody(
                            CreateExerciseRequestDto(
                                name = "Private Secret Drill",
                                description = "Private to owner",
                                categoryIds = listOf("Full Body"),
                                sharing = "PRIVATE",
                                recommendations = null
                            )
                        )
                    }.body<ExerciseDto>()

            // Viewer gets COMMUNITY exercise by ID -> 200 OK
            val getCommByIdRes =
                viewer.client.get("/api/v1/exercises/${communityEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${viewer.token}")
                }
            assertEquals(HttpStatusCode.OK, getCommByIdRes.status)
            assertEquals(communityEx.id, getCommByIdRes.body<ExerciseDto>().id)

            // Viewer gets PRIVATE exercise by ID -> 403 Forbidden
            val getPrivByIdRes =
                viewer.client.get("/api/v1/exercises/${privateEx.id}") {
                    header(HttpHeaders.Authorization, "Bearer ${viewer.token}")
                }
            assertEquals(HttpStatusCode.Forbidden, getPrivByIdRes.status)

            // Viewer lists exercises -> includes COMMUNITY, excludes PRIVATE
            val listRes =
                viewer.client.get("/api/v1/exercises?limit=100") {
                    header(HttpHeaders.Authorization, "Bearer ${viewer.token}")
                }
            assertEquals(HttpStatusCode.OK, listRes.status)
            val listedIds = listRes.body<PagedResponseDto<ExerciseDto>>().items.map { it.id }
            assertTrue(communityEx.id in listedIds)
            assertFalse(privateEx.id in listedIds)
        }

    @Test
    fun `create exercise preserves client id and deduplicates retries with scoped idempotency`() =
        testApplication {
            application { module() }
            val userA = createAuthenticatedUser("idemA@example.com", "IdemUserA")
            val userB = createAuthenticatedUser("idemB@example.com", "IdemUserB")

            val clientProvidedId = UUID.randomUUID().toString()
            val idemKey = "create-key-${UUID.randomUUID()}"

            val req =
                CreateExerciseRequestDto(
                    name = "Offline Created Exercise",
                    description = "Created on mobile with UUID",
                    categoryIds = listOf("Back"),
                    sharing = "PRIVATE",
                    recommendations = null,
                    id = clientProvidedId,
                    idempotencyKey = idemKey
                )

            val firstRes =
                userA.client.post("/api/v1/exercises") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(req)
                }
            assertEquals(HttpStatusCode.Created, firstRes.status)
            val firstDto = firstRes.body<ExerciseDto>()
            assertEquals(clientProvidedId, firstDto.id)

            // Retry with same idempotencyKey returns identical ExerciseDto
            val retryRes =
                userA.client.post("/api/v1/exercises") {
                    header(HttpHeaders.Authorization, "Bearer ${userA.token}")
                    contentType(ContentType.Application.Json)
                    setBody(req.copy(name = "Retry Name Should Be Ignored"))
                }
            assertEquals(HttpStatusCode.Created, retryRes.status)
            val retryDto = retryRes.body<ExerciseDto>()
            assertEquals(clientProvidedId, retryDto.id)
            assertEquals("Offline Created Exercise", retryDto.name)

            // Different user with same idempotencyKey does NOT get userA's exercise
            val userBRes =
                userB.client.post("/api/v1/exercises") {
                    header(HttpHeaders.Authorization, "Bearer ${userB.token}")
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateExerciseRequestDto(
                            name = "User B Exercise",
                            description = "Scoped key test",
                            categoryIds = listOf("Chest"),
                            sharing = "PRIVATE",
                            recommendations = null,
                            id = null,
                            idempotencyKey = idemKey
                        )
                    )
                }
            assertEquals(HttpStatusCode.Created, userBRes.status)
            val userBDto = userBRes.body<ExerciseDto>()
            assertEquals("User B Exercise", userBDto.name)
            assertEquals(userB.userId, userBDto.owner?.id)
        }

    @Test
    fun `pagination bounds clamp zero or negative page and limit`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser()

        user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(
                CreateExerciseRequestDto(
                    name = "Bounds Test Exercise",
                    description = "Testing pagination bounds",
                    categoryIds = emptyList(),
                    sharing = "PRIVATE",
                    recommendations = null
                )
            )
        }

        val res =
            user.client.get("/api/v1/exercises?page=0&limit=0") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
            }
        assertEquals(HttpStatusCode.OK, res.status)
        val paged = res.body<PagedResponseDto<ExerciseDto>>()
        assertEquals(1, paged.page)
        assertEquals(1, paged.limit)
        assertEquals(1, paged.items.size)

        val maxLimitRes =
            user.client.get("/api/v1/exercises?page=-5&limit=500") {
                header(HttpHeaders.Authorization, "Bearer ${user.token}")
            }
        assertEquals(HttpStatusCode.OK, maxLimitRes.status)
        val maxPaged = maxLimitRes.body<PagedResponseDto<ExerciseDto>>()
        assertEquals(1, maxPaged.page)
        assertEquals(100, maxPaged.limit)
    }

    @Test
    fun `create and update exercise persists hold time in recommendations`() = testApplication {
        application { module() }
        val user = createAuthenticatedUser("timer_user@example.com", "TimerUser")

        val createReq = CreateExerciseRequestDto(
            name = "Plank Hold",
            description = "Isometric core exercise",
            categoryIds = emptyList(),
            sharing = "PRIVATE",
            recommendations = listOf(
                ExerciseRecommendationDto(
                    level = "BEGINNER",
                    sets = 3,
                    reps = 1,
                    restTimeInMillis = 60_000L,
                    weightInKg = null,
                    timeInMillis = 30_000L
                )
            )
        )

        val createRes = user.client.post("/api/v1/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(createReq)
        }
        assertEquals(HttpStatusCode.Created, createRes.status)
        val created = createRes.body<ExerciseDto>()
        val begRec = created.recommendations?.firstOrNull { it.level == "BEGINNER" }
        assertNotNull(begRec)
        assertEquals(30_000L, begRec.timeInMillis)

        // Update recommendation hold time to 45s
        val updateReq = UpdateExerciseRequestDto(
            name = "Plank Hold",
            description = "Isometric core exercise - updated",
            categoryIds = emptyList(),
            sharing = "PRIVATE",
            expectedVersion = 1L,
            idempotencyKey = null,
            recommendations = listOf(
                ExerciseRecommendationDto(
                    level = "BEGINNER",
                    sets = 4,
                    reps = 1,
                    restTimeInMillis = 45_000L,
                    weightInKg = null,
                    timeInMillis = 45_000L
                )
            )
        )
        val updateRes = user.client.put("/api/v1/exercises/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
            contentType(ContentType.Application.Json)
            setBody(updateReq)
        }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updated = updateRes.body<ExerciseDto>()
        val updatedRec = updated.recommendations?.firstOrNull { it.level == "BEGINNER" }
        assertNotNull(updatedRec)
        assertEquals(45_000L, updatedRec.timeInMillis)

        // Verify sync response returns timeInMillis
        val syncRes = user.client.get("/api/v1/exercises/changes?sinceVersion=0") {
            header(HttpHeaders.Authorization, "Bearer ${user.token}")
        }
        assertEquals(HttpStatusCode.OK, syncRes.status)
        val syncData = syncRes.body<ExerciseSyncResponseDto>()
        val syncedExercise = syncData.changes.firstOrNull { it.id == created.id }
        assertNotNull(syncedExercise)
        assertEquals(45_000L, syncedExercise.recommendations?.firstOrNull { it.level == "BEGINNER" }?.timeInMillis)
    }
}
