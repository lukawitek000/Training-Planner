package com.lukasz.witkowski.training.planner.shared.time

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeTest {

    @Test
    fun `convert Time to timer string`() {
        // given
        val timeValues = timerStrings.keys

        timeValues.forEach { time ->
            // when
            val timerString = time.toTimerString()

            // then
            assertEquals(timerStrings[time], timerString)
        }
    }

    @Test
    fun `convert Time to string`() {
        // given
        val timeValues = timeStrings.keys

        timeValues.forEach { time ->
            // when
            val timeString = time.toString()

            // then
            assertEquals(timeStrings[time], timeString)
        }
    }

    @Test
    fun `calculate minutes and seconds from millis`() {
        // given
        val timeValues = minutesAndSeconds.keys

        timeValues.forEach { time ->
            // when
            val minutesSecondsPair = time.minutesAndSeconds()

            // then
            assertEquals(minutesAndSeconds[time], minutesSecondsPair)
        }
    }

    private val timerStrings = mapOf(
        Time(minutes = 3, seconds = 10) to "3:10.0",
        Time(timeInMillis = 365300) to "6:05.3",
        Time(timeInMillis = 45300) to "45.3",
        Time(timeInMillis = 500) to "0.5",
        Time(seconds = 4) to "4.0",
        Time(minutes = 3, seconds = 0) to "3:00.0",
        Time(timeInMillis = 0) to "0.0"
    )

    private val timeStrings = mapOf(
        Time(minutes = 3, seconds = 10) to "3min 10s",
        Time(timeInMillis = 365300) to "6min 5s",
        Time(timeInMillis = 45300) to "45s",
        Time(timeInMillis = 0) to "0s",
        Time(seconds = 4) to "4s",
        Time(minutes = 3, seconds = 0) to "3min",
        Time(hour = 2, minutes = 9, seconds = 3) to "2h 9min",
        Time(hour = 5, seconds = 0) to "5h"
    )

    private val minutesAndSeconds = mapOf(
        Time(623_000) to Pair(10, 23),
        Time(480_000) to Pair(8, 0),
        Time(10_000) to Pair(0, 10),
        Time(0) to Pair(0, 0),
        Time(60_000) to Pair(1, 0)
    )
}
