package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationFormatterTest {

    @Test
    fun `zero duration decomposes to single zero seconds part`() {
        val result = Duration.ZERO.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(0L, DurationUnitPart.SECONDS)),
            result.parts
        )
    }

    @Test
    fun `sub-second duration below half second rounds down to zero`() {
        val result = 499.milliseconds.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(0L, DurationUnitPart.SECONDS)),
            result.parts
        )
    }

    @Test
    fun `sub-second duration at or above half second rounds up to one second`() {
        val result = 500.milliseconds.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(1L, DurationUnitPart.SECONDS)),
            result.parts
        )
    }

    @Test
    fun `59_7 seconds rounds to 1 minute without 60s overflow`() {
        val result = 59_700.milliseconds.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(1L, DurationUnitPart.MINUTES)),
            result.parts
        )
    }

    @Test
    fun `3599_6 seconds rounds to 1 hour without 60m overflow`() {
        val result = 3_599_600.milliseconds.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(1L, DurationUnitPart.HOURS)),
            result.parts
        )
    }

    @Test
    fun `multi-unit duration omits zero components`() {
        val result = (1.hours + 5.seconds).decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(
                DurationPart(1L, DurationUnitPart.HOURS),
                DurationPart(5L, DurationUnitPart.SECONDS)
            ),
            result.parts
        )
    }

    @Test
    fun `full hours minutes and seconds are preserved in descending order`() {
        val result = (2.hours + 15.minutes + 42.seconds).decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(
                DurationPart(2L, DurationUnitPart.HOURS),
                DurationPart(15L, DurationUnitPart.MINUTES),
                DurationPart(42L, DurationUnitPart.SECONDS)
            ),
            result.parts
        )
    }

    @Test
    fun `duration over 24 hours stays in total hours`() {
        val result = (25.hours + 30.minutes).decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(
                DurationPart(25L, DurationUnitPart.HOURS),
                DurationPart(30L, DurationUnitPart.MINUTES)
            ),
            result.parts
        )
    }

    @Test
    fun `negative duration preserves negative flag with positive parts`() {
        val result = (-(1.minutes + 30.seconds)).decomposeForDisplay()

        assertTrue(result.isNegative)
        assertEquals(
            listOf(
                DurationPart(1L, DurationUnitPart.MINUTES),
                DurationPart(30L, DurationUnitPart.SECONDS)
            ),
            result.parts
        )
    }

    @Test
    fun `negative sub-second duration rounding to zero is not negative`() {
        val result = (-200).milliseconds.decomposeForDisplay()

        assertFalse(result.isNegative)
        assertEquals(
            listOf(DurationPart(0L, DurationUnitPart.SECONDS)),
            result.parts
        )
    }
}
