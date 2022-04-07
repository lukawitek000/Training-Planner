package com.lukasz.witkowski.training.planner.statistics.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CountdownTimer {

    private var isTimerRunning = false
    private var value = 0L
    private var counterStep = 0L


    fun start(time: Long, step: Long): Flow<Long> = flow {
        value = time
        isTimerRunning = true
        counterStep = step
        while (isTimerRunning && value >= 0L) {
            emit(value)
            value -= step
            delay(step)
        }
    }

    fun stop() {
        isTimerRunning = false
    }

    fun resume() {
        start(value, counterStep)
    }

    fun pause() {
        stop()
    }
}