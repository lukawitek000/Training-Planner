package com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import java.util.Date

interface TimeProvider {
    /**
     * Returns the difference between the current time and midnight, January 1, 1970 UTC.
     */
    fun currentTime(): Time

    fun currentDate(): Date
}
