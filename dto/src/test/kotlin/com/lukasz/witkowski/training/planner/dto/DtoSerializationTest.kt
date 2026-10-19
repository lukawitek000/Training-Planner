package com.lukasz.witkowski.training.planner.dto

import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.OwnerDto
import kotlin.test.assertEquals
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
                revision = 1L
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
                revision = 2L
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
                "futureField": "some future value"
            }
            """.trimIndent()

        val decoded = json.decodeFromString(ExerciseDto.serializer(), jsonStringWithExtraField)

        assertEquals("ex-1", decoded.id)
        assertEquals("Push-up", decoded.name)
    }
}
