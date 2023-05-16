package com.lukasz.witkowski.training.planner.shared.time

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeTest {

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

    private val minutesAndSeconds = mapOf(
        Time(623_000) to Pair(10, 23),
        Time(480_000) to Pair(8, 0),
        Time(10_000) to Pair(0, 10),
        Time(0) to Pair(0, 0),
        Time(60_000) to Pair(1, 0)
    )
}
