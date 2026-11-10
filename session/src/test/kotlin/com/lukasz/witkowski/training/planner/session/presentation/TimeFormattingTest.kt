package com.lukasz.witkowski.training.planner.session.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class TimeFormattingTest {

    @Test
    fun `positive time is correctly formatted with minutes and seconds`() {
        val result = formatRemainingTime(75.seconds)
        assertEquals("01:15", result)
    }

    @Test
    fun `positive time is correctly formatted with only seconds`() {
        val result = formatRemainingTime(10.seconds)
        assertEquals("00:10", result)
    }

    @Test
    fun `positive time is correctly formatted with only minutes`() {
        val result = formatRemainingTime(60.seconds)
        assertEquals("01:00", result)
    }

    @Test
    fun `negative time is correctly formatted with minutes and seconds`() {
        val result = formatRemainingTime((-75).seconds)
        assertEquals("01:15", result)
    }

    @Test
    fun `negative time is correctly formatted with only seconds`() {
        val result = formatRemainingTime((-10).seconds)
        assertEquals("00:10", result)
    }

    @Test
    fun `negative time is correctly formatted with only minutes`() {
        val result = formatRemainingTime((-60).seconds)
        assertEquals("01:00", result)
    }
}