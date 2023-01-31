package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import org.junit.Test
import kotlin.test.assertEquals

class DefaultTimerControllerTest {

    private val timerController = DefaultTimerController()

    @Test
    fun `initialized timer controller has the time set to 0`() {
        assertEquals(Time.ZERO, timerController.timer.value)
    }
}