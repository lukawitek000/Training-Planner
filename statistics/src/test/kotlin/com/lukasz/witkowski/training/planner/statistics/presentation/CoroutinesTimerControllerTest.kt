package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.shared.time.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutinesTimerControllerTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)
    private lateinit var timerController: TimerController

    @Test
    fun `initialized timer controller has the time set to 0, is not finished and not running`() {
        givenTimerController()

        assertEquals(Time.ZERO, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `timer is equal to the set value`() {
        givenTimerController()
        val time5min = Time(minutes = 5, seconds = 0)

        timerController.setTimer(time5min)
        assertEquals(time5min, timerController.timer.value)
        assertEquals(false, timerController.hasFinished.value)
        assertEquals(false, timerController.isRunning.value)
    }

    @Test
    fun `first emitted time is equal to the initial value`() = runTest(testDispatcher) {
        givenTimerController()
        val time = Time(seconds = 5)

        timerController.setTimer(time)
        timerController.startTimer()
        val firstTime = timerController.timer.first()
        assertEquals(time, firstTime)
    }

    @Test
    fun `timer emits 3 values withing 3 seconds`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 20)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        timerController.startTimer()
        val job = launch {
            timerController.timer.toList(testResults)
        }
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS)
        assertContentEquals(listOf(Time(seconds = 20), Time(seconds = 19), Time(seconds = 18)), testResults)
        assertFalse(timerController.hasFinished.value)
        assertTrue(timerController.isRunning.value)
        timerController.stopTimer()
        job.cancel()
    }

    @Test
    fun `timer stops emitting after 3 seconds and reset to initial value`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 20)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        val job = launch {
            timerController.timer.toList(testResults)
        }
        timerController.startTimer()

        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timerController.stopTimer()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS)

        assertContentEquals(listOf(Time(seconds = 20), Time(seconds = 19), Time(seconds = 18), Time(seconds = 17), Time(seconds = 20)), testResults)
        assertFalse(timerController.hasFinished.value)
        assertFalse(timerController.isRunning.value)
        job.cancel()
    }

    @Test
    fun `timer emits 3 seconds and is finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        val job = launch {
            timerController.timer.toList(testResults)
        }
        timerController.startTimer()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 3), Time(seconds = 2), Time(seconds = 1), Time(seconds = 0)), testResults)
        assertTrue(timerController.hasFinished.value)
        assertFalse(timerController.isRunning.value)
        job.cancel()
    }

    @Test
    fun `start timer after it has finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val testResults = mutableListOf<Time>()

        timerController.setTimer(time)
        val job = launch {
            timerController.timer.toList(testResults)
        }
        timerController.startTimer()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 3), Time(seconds = 2), Time(seconds = 1), Time(seconds = 0)), testResults)
        assertTrue(timerController.hasFinished.value)
        assertFalse(timerController.isRunning.value)
        job.cancel()


        timerController.resetTimer()
        val testResults2 = mutableListOf<Time>()
        val job2 = launch {
            timerController.timer.toList(testResults2)
        }
        timerController.startTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timerController.stopTimer()

        assertContentEquals(listOf(Time(seconds = 3), Time(seconds = 2), Time(seconds = 1)), testResults2)
        assertFalse(timerController.hasFinished.value)
        assertFalse(timerController.isRunning.value)
        job2.cancel()
    }

    @Test
    fun `stop not started timer has not effect`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timerController.timer.toList(resultList)
        }
        timerController.setTimer(time)
        scheduler.advanceTimeBy( MILLIS_TO_EMIT_LAST_VALUE)

        timerController.stopTimer()
        scheduler.advanceTimeBy( MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(time), resultList)
        job.cancel()
    }

    @Test
    fun `reset timer when it is running`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timerController.timer.toList(resultList)
        }
        timerController.setTimer(time)
        timerController.startTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        timerController.resetTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 5), Time(seconds = 4), Time(seconds = 3), Time(seconds = 5)), resultList)
        assertFalse(timerController.isRunning.value)
        assertFalse(timerController.hasFinished.value)
        job.cancel()
    }

    @Test
    fun `after pause new values are not emitted`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timerController.timer.toList(resultList)
        }
        timerController.setTimer(time)
        timerController.startTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        timerController.pauseTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 5), Time(seconds = 4), Time(seconds = 3)), resultList)
        assertFalse(timerController.isRunning.value)
        assertFalse(timerController.hasFinished.value)
        job.cancel()
    }

    @Test
    fun `resume after pause emits consecutive time till finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timerController.timer.toList(resultList)
        }
        timerController.setTimer(time)
        timerController.startTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        timerController.pauseTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timerController.resumeTimer()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 5), Time(seconds = 4), Time(seconds = 3), Time(seconds = 2), Time(seconds = 1), Time(seconds = 0)), resultList)
        assertFalse(timerController.isRunning.value)
        assertTrue(timerController.hasFinished.value)
        job.cancel()
    }

    @Test
    fun `resume start timer if it was not started yet`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timerController.timer.toList(resultList)
        }
        timerController.setTimer(time)
        timerController.resumeTimer()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timerController.stopTimer()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(Time(seconds = 5), Time(seconds = 4), Time(seconds = 3), Time(seconds = 5)), resultList)
        assertFalse(timerController.isRunning.value)
        assertFalse(timerController.hasFinished.value)
        job.cancel()
    }

    private fun givenTimerController(tickDelayInMillis: Long = SECOND_IN_MILLIS) {
        timerController = CoroutinesTimerController(tickDelayInMillis, testDispatcher)
    }

    companion object {
        const val SECOND_IN_MILLIS = 1000L
        const val MILLIS_TO_EMIT_LAST_VALUE = 1L // The time has to be slightly over the delay time to emit the last value
    }
}
