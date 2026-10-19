package com.lukasz.witkowski.training.planner.dto

import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.OwnerDto
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json
import org.junit.Test

class DtoSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `exerciseDto serialization and deserialization works correctly`() {
        val exercise =
            ExerciseDto(
                id = "ex-1",
                name = "Push-up",
                description = "Standard push-up",
                categories = listOf(CategoryDto("cat-1", "Chest")),
                owner = null,
                source = "BUNDLED",
                sharing = "PRIVATE",
                revision = 1L,
                recommendations = null
            )

        val encoded = json.encodeToString(ExerciseDto.serializer(), exercise)
        val decoded = json.decodeFromString(ExerciseDto.serializer(), encoded)

        assertEquals(exercise, decoded)
    }

    @Test
    fun `exerciseDto with owner serialization and deserialization works correctly`() {
        val exercise =
            ExerciseDto(
                id = "ex-1",
                name = "Push-up",
                description = "Standard push-up",
                categories = listOf(CategoryDto("cat-1", "Chest")),
                owner = OwnerDto("user-1", "john_doe"),
                source = "USER_CREATED",
                sharing = "COMMUNITY",
                revision = 2L,
                recommendations = null
            )

        val encoded = json.encodeToString(ExerciseDto.serializer(), exercise)
        val decoded = json.decodeFromString(ExerciseDto.serializer(), encoded)

        assertEquals(exercise, decoded)
    }

    @Test
    fun `deserialization ignores unknown keys for backward compatibility`() {
        val jsonStringWithExtraField =
            """
            {
                "id": "ex-1",
                "name": "Push-up",
                "description": "Standard push-up",
                "categories": [],
                "owner": null,
                "source": "BUNDLED",
                "sharing": "PRIVATE",
                "revision": 1,
                "recommendations": null,
                "futureField": "some future value"
            }
            """.trimIndent()

        val decoded = json.decodeFromString(ExerciseDto.serializer(), jsonStringWithExtraField)

        assertEquals("ex-1", decoded.id)
        assertEquals("Push-up", decoded.name)
    }

    @Test
    fun `createExerciseRequestDto serialization with id and idempotencyKey works correctly`() {
        val request =
            CreateExerciseRequestDto(
                name = "Bench Press",
                description = "Barbell chest exercise",
                categoryIds = listOf("Chest"),
                sharing = "COMMUNITY",
                recommendations = null,
                id = "client-ex-123",
                idempotencyKey = "idem-key-456"
            )

        val encoded = json.encodeToString(CreateExerciseRequestDto.serializer(), request)
        val decoded = json.decodeFromString(CreateExerciseRequestDto.serializer(), encoded)

        assertEquals(request, decoded)
        assertEquals("client-ex-123", decoded.id)
        assertEquals("idem-key-456", decoded.idempotencyKey)
    }

    @Test
    fun `createExerciseRequestDto serialization without id and idempotencyKey defaults to null`() {
        val request =
            CreateExerciseRequestDto(
                name = "Bench Press",
                description = "Barbell chest exercise",
                categoryIds = listOf("Chest"),
                sharing = "PRIVATE",
                recommendations = null
            )

        val encoded = json.encodeToString(CreateExerciseRequestDto.serializer(), request)
        val decoded = json.decodeFromString(CreateExerciseRequestDto.serializer(), encoded)

        assertEquals(request, decoded)
        assertNull(decoded.id)
        assertNull(decoded.idempotencyKey)

        val rawJsonWithoutOptionalFields =
            """
            {
                "name": "Squat",
                "description": "Leg exercise",
                "categoryIds": ["Legs"],
                "sharing": "PRIVATE",
                "recommendations": null
            }
            """.trimIndent()
        val decodedFromRaw =
            json.decodeFromString(CreateExerciseRequestDto.serializer(), rawJsonWithoutOptionalFields)
        assertEquals("Squat", decodedFromRaw.name)
        assertNull(decodedFromRaw.id)
        assertNull(decodedFromRaw.idempotencyKey)
    }
}
