package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTimerControllerTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)
    private lateinit var timerController: TimerController

    @Test
    fun `initialized timer controller has the time set to 0, is not finished and not running`() {
        givenTrainingController()

        assertEquals(Time.ZERO, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `timer is equal to the set value`() {
        givenTrainingController()
        val time5min = Time(minutes = 5, seconds = 0)

        timerController.setTimer(time5min)
        assertEquals(time5min, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `first emitted time is equal to the initial value`() = runTest(testDispatcher) {
        givenTrainingController()
        val time = Time(seconds = 5)

        timerController.setTimer(time)
        timerController.startTimer()
        val firstTime = timerController.timer.first()
        assertEquals(time, firstTime)
    }

    @Test
    fun `timer emits 3 values withing 3 seconds`() = runTest(testDispatcher) {
        givenTrainingController(SECOND_IN_MILLIS)
        val time = Time(seconds = 20)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        timerController.startTimer()
        val job = launch {
            timerController.timer.toList(testResults)
        }
        scheduler.advanceTimeBy(3000L)
        assertContentEquals(listOf(Time(seconds = 20), Time(seconds = 19), Time(seconds = 18)), testResults)
        timerController.stopTimer()
        job.cancel()
    }

    @Test
    fun `timer stops emitting after 3 seconds and reset to initial value`() = runTest(testDispatcher) {
        givenTrainingController(SECOND_IN_MILLIS)
        val time = Time(seconds = 20)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        timerController.startTimer()
        val job = launch {
            timerController.timer.toList(testResults)
        }
        scheduler.advanceTimeBy(3000L)
        timerController.stopTimer()
        scheduler.advanceTimeBy(3000L)
        assertContentEquals(listOf(Time(seconds = 20), Time(seconds = 19), Time(seconds = 18), Time(seconds = 20)), testResults)
        job.cancel()
    }

    private fun givenTrainingController(tickDelayInMillis: Long = SECOND_IN_MILLIS) {
        timerController = DefaultTimerController(tickDelayInMillis, testDispatcher)
    }

    companion object {
        const val SECOND_IN_MILLIS = 1000L
    }
}
