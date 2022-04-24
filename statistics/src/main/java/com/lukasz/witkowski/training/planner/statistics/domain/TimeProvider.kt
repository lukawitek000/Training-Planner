package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time

interface TimeProvider {
    /**
     * Returns the difference between the current time and midnight, January 1, 1970 UTC.
     */
    fun currentTime(): Time
}