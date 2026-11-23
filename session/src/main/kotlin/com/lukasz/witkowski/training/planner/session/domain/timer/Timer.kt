package com.lukasz.witkowski.training.planner.session.domain.timer

import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Timer(
    initialState: TimerState,
    private val allowOverflow: Boolean = false,
    private val tickDelayInMillis: Duration = TICK_DELAY,
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : AutoCloseable {
    private val coroutineScope: CoroutineScope =
        CoroutineScope(timerDispatcher + CoroutineName("Timer"))
    private var timerJob: Job? = null
    private var initialTime: Duration = Duration.ZERO

    val state: StateFlow<TimerState>
        field = MutableStateFlow<TimerState>(initialState)

    fun start() {
        state.value.let {
            check(it is TimerState.Ready) {
                "Cannot start the Timer it is already running"
            }
            start(it.duration)
        }
    }

    fun changeTime(duration: Duration) {
        timerJob?.cancel()
        initialTime = duration
        state.value = TimerState.Ready(duration)
    }

    fun reset() {
        timerJob?.cancel()
        state.value = TimerState.Ready(initialTime)
    }

    override fun close() {
        coroutineScope.cancel()
    }

    private fun start(initTime: Duration) {
        initialTime = initTime
        val startTimestampMillis = timeProvider.elapsedTimeMillis()
        state.update { TimerState.Running(initTime) }
        timerJob =
            coroutineScope.launch {
                while (
                    state.value is TimerState.Running ||
                    state.value is TimerState.OverflowRunning
                ) {
                    delay(tickDelayInMillis)
                    val elapsed =
                        (timeProvider.elapsedTimeMillis() - startTimestampMillis).milliseconds
                    val remaining = initTime - elapsed
                    state.update {
                        if (remaining.isNegative()) {
                            if (allowOverflow) {
                                TimerState.OverflowRunning(remaining)
                            } else {
                                TimerState.Finished
                            }
                        } else if (remaining == Duration.ZERO && !allowOverflow) {
                            TimerState.Finished
                        } else {
                            TimerState.Running(remaining)
                        }
                    }
                }
            }
    }

    private companion object {
        val TICK_DELAY = 100.milliseconds
    }
}
