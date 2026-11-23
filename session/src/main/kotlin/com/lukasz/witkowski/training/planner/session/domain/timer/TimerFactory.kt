package com.lukasz.witkowski.training.planner.session.domain.timer

import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class TimerFactory(
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val tickDelay: Duration = DEFAULT_TICK_DELAY,
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    fun create(initialState: TimerState, allowOverflow: Boolean = false): Timer = Timer(
        initialState = initialState,
        allowOverflow = allowOverflow,
        tickDelayInMillis = tickDelay,
        timeProvider = timeProvider,
        timerDispatcher = timerDispatcher
    )

    private companion object {
        val DEFAULT_TICK_DELAY = 100.milliseconds
    }
}
