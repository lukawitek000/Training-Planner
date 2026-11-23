package com.lukasz.witkowski.training.planner.dto.serializers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class IsoInstantSerializerTest {

    @Serializable
    private data class TimestampWrapper(
        @Serializable(with = IsoInstantSerializer::class)
        val recordedAt: Instant
    )

    @Test
    fun `round trips Instant through ISO-8601 UTC string`() {
        val original = TimestampWrapper(Instant.parse("2026-11-20T20:23:50Z"))

        val encoded = Json.encodeToString(original)
        assertEquals("""{"recordedAt":"2026-11-20T20:23:50Z"}""", encoded)

        val decoded = Json.decodeFromString<TimestampWrapper>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `deserializes fractional seconds and non-UTC offset`() {
        val decoded = Json.decodeFromString<TimestampWrapper>(
            """{"recordedAt":"2026-11-20T22:23:50.123+02:00"}"""
        )
        assertEquals(Instant.parse("2026-11-20T20:23:50.123Z"), decoded.recordedAt)
    }

    @Test
    fun `throws SerializationException on malformed instant string`() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString<TimestampWrapper>("""{"recordedAt":"not-a-timestamp"}""")
        }
    }
}
