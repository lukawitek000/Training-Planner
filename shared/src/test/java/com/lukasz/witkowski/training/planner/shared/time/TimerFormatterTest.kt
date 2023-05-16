package com.lukasz.witkowski.training.planner.shared.time

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class TimerFormatterTest(
    private val time: Time,
    private val includeTenthSecond: Boolean,
    private val expectedString: String
) {

    private val resourcesProvider = TestResourcesProvider()
    private val timeFormatter = TimeFormatter(resourcesProvider)

    @Test
    fun `test timer formatting`() {
        val actual = timeFormatter.formatTimer(time, includeTenthSecond)
        assertEquals(expectedString, actual)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(Time(minutes = 10, seconds = 1), false, "10:01"),
                arrayOf(Time(minutes = 10, seconds = 1), true, "10:01.0"),
                arrayOf(Time(minutes = 10, seconds = 11), true, "10:11.0"),
                arrayOf(Time(minutes = 1, seconds = 11), true, "1:11.0"),
                arrayOf(Time(minutes = 0, seconds = 2), true, "2.0"),
                arrayOf(Time(minutes = 0, seconds = 2), false, "2"),
                arrayOf(Time(minutes = 0, seconds = 23), false, "23"),
                arrayOf(Time(123_300), true, "2:03.3"),
                arrayOf(Time(123_457), true, "2:03.4"),
                arrayOf(Time(123_499), true, "2:03.4")
            )
        }
    }
}
