package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import java.util.Date

interface TimeProvider {
    /**
     * Returns the difference between the current time and midnight, January 1, 1970 UTC.
     */
    fun currentTime(): Long

    fun currentDate(): Date

    fun elapsedTimeMillis(): Long
}
