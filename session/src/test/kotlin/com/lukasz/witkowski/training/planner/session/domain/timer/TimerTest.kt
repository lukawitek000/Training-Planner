package com.lukasz.witkowski.training.planner.session.domain.timer

import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerTest {
    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)
    private val timeProvider = FixedTimeProvider()

    @Test
    fun `initialized timer with NotSet starts in NotSet state`() {
        val timer = createTimer(TimerState.NotSet)

        assertEquals(TimerState.NotSet, timer.state.value)
        timer.close()
    }

    @Test
    fun `initialized timer with Ready holds initial duration`() {
        val timer = createTimer(TimerState.Ready(5.seconds))

        assertEquals(TimerState.Ready(5.seconds), timer.state.value)
        timer.close()
    }

    @Test
    fun `starting timer when not in Ready state throws IllegalStateException`() {
        val timer = createTimer(TimerState.NotSet)

        assertFailsWith<IllegalStateException> {
            timer.start()
        }
        timer.close()
    }

    @Test
    fun `timer counts down and transitions to Finished when allowOverflow is false`() =
        runTest(testDispatcher) {
            val timer = createTimer(TimerState.Ready(3.seconds), allowOverflow = false)
            val states = mutableListOf<TimerState>()
            val collectJob =
                backgroundScope.launch(UnconfinedTestDispatcher(scheduler)) {
                    timer.state.toList(states)
                }

            timer.start()
            repeat(3) {
                timeProvider.advanceBy(1.seconds)
                scheduler.advanceTimeBy(1000L)
                scheduler.runCurrent()
            }

            assertContentEquals(
                listOf(
                    TimerState.Ready(3.seconds),
                    TimerState.Running(3.seconds),
                    TimerState.Running(2.seconds),
                    TimerState.Running(1.seconds),
                    TimerState.Finished
                ),
                states
            )
            timer.close()
            collectJob.cancel()
        }

    @Test
    fun `timer transitions to OverflowRunning when allowOverflow is true`() =
        runTest(testDispatcher) {
            val timer = createTimer(TimerState.Ready(2.seconds), allowOverflow = true)

            timer.start()
            timeProvider.advanceBy(3.seconds)
            scheduler.advanceTimeBy(1000L)
            scheduler.runCurrent()

            assertEquals(TimerState.OverflowRunning((-1).seconds), timer.state.value)
            timer.close()
        }

    @Test
    fun `changeTime cancels running timer and sets state back to Ready`() =
        runTest(testDispatcher) {
            val timer = createTimer(TimerState.Ready(10.seconds))
            timer.start()

            timeProvider.advanceBy(2.seconds)
            scheduler.advanceTimeBy(1000L)
            scheduler.runCurrent()
            assertEquals(TimerState.Running(8.seconds), timer.state.value)

            timer.changeTime(20.seconds)
            assertEquals(TimerState.Ready(20.seconds), timer.state.value)
            timer.close()
        }

    private fun createTimer(initialState: TimerState, allowOverflow: Boolean = false): Timer =
        Timer(
            initialState = initialState,
            allowOverflow = allowOverflow,
            tickDelayInMillis = 1.seconds,
            timeProvider = timeProvider,
            timerDispatcher = testDispatcher
        )
}
