package com.lukasz.witkowski.training.planner.shared.time

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
@RunWith(value = Parameterized::class)
class TimeFormatterTest(
    private val time: Time,
    private val expectedString: String
) {

    private val resourcesProvider = TestResourcesProvider()
    private val timeFormatter = TimeFormatter(resourcesProvider)

    @Test
    fun `test time formatting`() {
        val actual = timeFormatter.formatTime(time)
        assertEquals(expectedString, actual)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(Time(minutes = 10, seconds = 1), "10min 1s"),
                arrayOf(Time(minutes = 10, seconds = 11), "10min 11s"),
                arrayOf(Time(minutes = 1, seconds = 11), "1min 11s"),
                arrayOf(Time(minutes = 0, seconds = 2), "2s"),
                arrayOf(Time(minutes = 0, seconds = 23), "23s"),
                arrayOf(Time(123_300), "2min 3s"),
                arrayOf(Time(123_457), "2min 3s"),
                arrayOf(Time(123_499), "2min 3s"),
                arrayOf(Time(0), "0s"),
                arrayOf(Time(hour = 2, minutes = 10, seconds = 9), "2h 10min"),
                arrayOf(Time(hour = 2, minutes = 5, seconds = 0), "2h 5min"),
                arrayOf(Time(hour = 2, minutes = 65, seconds = 0), "3h 5min"),
                arrayOf(Time(hour = 2, minutes = 0, seconds = 0), "2h"),
            )
        }
    }
}