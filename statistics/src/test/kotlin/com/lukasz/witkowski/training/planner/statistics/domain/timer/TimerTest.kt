package com.lukasz.witkowski.training.planner.statistics.domain.timer

import com.lukasz.witkowski.training.planner.shared.time.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)
    private lateinit var timer: Timer

    @Before
    fun setUp() {
        givenTimerController(SECOND_IN_MILLIS)
    }

    @Test
    fun `initialized timer has the time set to 0, with flags set to false`() {
        givenTimerController()

        assertEquals(Time.ZERO, timer.time.value)
        assertFalse(timer.hasFinished.value)
        assertFalse(timer.isRunning.value)
        assertFalse(timer.isPaused)
    }

    @Test
    fun `timer is equal to the set value`() {
        givenTimerController()
        val time5min = Time(minutes = 5, seconds = 0)

        timer.setTime(time5min)
        assertEquals(time5min, timer.time.value)
        assertFalse(timer.hasFinished.value)
        assertFalse(timer.isRunning.value)
        assertFalse(timer.isPaused)
    }

    @Test
    fun `first emitted time is equal to the initial value`() = runTest(testDispatcher) {
        givenTimerController()
        val time = Time(seconds = 5)

        timer.setTime(time)
        timer.start()
        val firstTime = timer.time.first()
        assertEquals(time, firstTime)
    }

    @Test
    fun `timer emits 3 values withing 3 seconds`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 20)
        val testResults = mutableListOf<Time>()

        timer.setTime(time)
        timer.start()
        val job = launch {
            timer.time.toList(testResults)
        }
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS)
        assertContentEquals(
            listOf(Time(seconds = 20), Time(seconds = 19), Time(seconds = 18)),
            testResults
        )
        assertFalse(timer.hasFinished.value)
        assertTrue(timer.isRunning.value)
        assertFalse(timer.isPaused)
        timer.stop()
        job.cancel()
    }

    @Test
    fun `timer stops emitting after 3 seconds and reset to initial value`() =
        runTest(testDispatcher) {
            givenTimerController(SECOND_IN_MILLIS)
            val time = Time(seconds = 20)
            val testResults = mutableListOf<Time>()

            timer.setTime(time)
            val job = launch {
                timer.time.toList(testResults)
            }
            timer.start()

            scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
            timer.stop()
            scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS)

            assertContentEquals(
                listOf(
                    Time(seconds = 20),
                    Time(seconds = 19),
                    Time(seconds = 18),
                    Time(seconds = 17),
                    Time(seconds = 20)
                ), testResults
            )
            assertFalse(timer.hasFinished.value)
            assertFalse(timer.isRunning.value)
            assertFalse(timer.isPaused)
            job.cancel()
        }

    @Test
    fun `timer emits 3 seconds and is finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val testResults = mutableListOf<Time>()

        timer.setTime(time)
        val job = launch {
            timer.time.toList(testResults)
        }
        timer.start()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(
            listOf(
                Time(seconds = 3),
                Time(seconds = 2),
                Time(seconds = 1),
                Time(seconds = 0)
            ), testResults
        )
        assertTrue(timer.hasFinished.value)
        assertFalse(timer.isRunning.value)
        assertFalse(timer.isPaused)
        job.cancel()
    }

    @Test
    fun `start timer after it has finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val testResults = mutableListOf<Time>()

        timer.setTime(time)
        val job = launch {
            timer.time.toList(testResults)
        }
        timer.start()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(
            listOf(
                Time(seconds = 3),
                Time(seconds = 2),
                Time(seconds = 1),
                Time(seconds = 0)
            ), testResults
        )
        assertTrue(timer.hasFinished.value)
        assertFalse(timer.isRunning.value)
        assertFalse(timer.isPaused)
        job.cancel()

        val testResults2 = mutableListOf<Time>()
        timer.start()
        val job2 = launch {
            timer.time.toList(testResults2)
        }
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timer.stop()

        assertContentEquals(
            listOf(Time(seconds = 3), Time(seconds = 2), Time(seconds = 1)),
            testResults2
        )
        assertFalse(timer.hasFinished.value)
        assertFalse(timer.isRunning.value)
        assertFalse(timer.isPaused)
        job2.cancel()
    }

    @Test
    fun `stop not started timer has not effect`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 3)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timer.time.toList(resultList)
        }
        timer.setTime(time)
        scheduler.advanceTimeBy(MILLIS_TO_EMIT_LAST_VALUE)

        timer.stop()
        scheduler.advanceTimeBy(MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(listOf(time), resultList)
        job.cancel()
    }

    @Test
    fun `after pause new values are not emitted`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timer.time.toList(resultList)
        }
        timer.setTime(time)
        timer.start()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        timer.pause()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(
            listOf(Time(seconds = 5), Time(seconds = 4), Time(seconds = 3)),
            resultList
        )
        assertFalse(timer.isRunning.value)
        assertFalse(timer.hasFinished.value)
        assertTrue(timer.isPaused)
        job.cancel()
    }

    @Test
    fun `resume after pause emits consecutive time till finished`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timer.time.toList(resultList)
        }
        timer.setTime(time)
        timer.start()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        timer.pause()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        assertTrue(timer.isPaused)
        timer.resume()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(
            listOf(
                Time(seconds = 5),
                Time(seconds = 4),
                Time(seconds = 3),
                Time(seconds = 2),
                Time(seconds = 1),
                Time(seconds = 0)
            ), resultList
        )
        assertFalse(timer.isRunning.value)
        assertTrue(timer.hasFinished.value)
        assertFalse(timer.isPaused)
        job.cancel()
    }

    @Test
    fun `resume start timer if it was not started yet`() = runTest(testDispatcher) {
        givenTimerController(SECOND_IN_MILLIS)
        val time = Time(seconds = 5)
        val resultList = mutableListOf<Time>()
        val job = launch {
            timer.time.toList(resultList)
        }
        timer.setTime(time)
        timer.resume()
        scheduler.advanceTimeBy(2 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)
        timer.stop()
        scheduler.advanceTimeBy(3 * SECOND_IN_MILLIS + MILLIS_TO_EMIT_LAST_VALUE)

        assertContentEquals(
            listOf(
                Time(seconds = 5),
                Time(seconds = 4),
                Time(seconds = 3),
                Time(seconds = 5)
            ), resultList
        )
        assertFalse(timer.isRunning.value)
        assertFalse(timer.hasFinished.value)
        assertFalse(timer.isPaused)
        job.cancel()
    }

    private fun givenTimerController(tickDelayInMillis: Long = SECOND_IN_MILLIS) {
        timer = Timer(tickDelayInMillis, testDispatcher)
    }

    companion object {
        const val SECOND_IN_MILLIS = 1000L
        const val MILLIS_TO_EMIT_LAST_VALUE =
            1L // The time has to be slightly over the delay time to emit the last value
    }
}
