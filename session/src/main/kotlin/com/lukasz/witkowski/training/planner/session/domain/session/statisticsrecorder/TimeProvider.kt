package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface TimeProvider {
    /**
     * Returns the difference between the current time and midnight, January 1, 1970 UTC.
     */
    fun currentTime(): Long

    fun currentDate(): Date

    fun elapsedTimeMillis(): Long

    /**
     * Monotonic duration elapsed since device boot.
     */
    fun elapsedTime(): Duration = elapsedTimeMillis().milliseconds
}
