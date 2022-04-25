package com.lukasz.witkowski.shared.time

import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class TimeTest {

    @Test
    fun `convert Time to timer string`() {
        // given
        val timeValues = timerStrings.keys

        timeValues.forEach{ time ->
            // when
            val timerString = time.toTimerString()

            // then
            assertEquals(timerStrings[time], timerString)
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

}