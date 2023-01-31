package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTimerControllerTest {

    private val timerController = DefaultTimerController()

    @Test
    fun `initialized timer controller has the time set to 0, is not finished and not running`() {
        assertEquals(Time.ZERO, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `timer is equal to the set value`() {
        val time5min = Time(minutes = 5, seconds = 0)
        timerController.setTimer(time5min)
        assertEquals(time5min, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `first emitted time is equal to the initial value`() = runTest {
        val time = Time(seconds = 5)
        timerController.setTimer(time)
        timerController.startTimer()
        val firstTime = timerController.timer.first()
        assertEquals(time, firstTime)
    }
}
