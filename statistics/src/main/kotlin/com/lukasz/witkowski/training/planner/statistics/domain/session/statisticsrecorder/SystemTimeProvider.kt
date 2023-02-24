package com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.shared.time.Time
import java.util.Date

class SystemTimeProvider : TimeProvider {
    override fun currentTime(): Time {
        val timeInMillis = System.currentTimeMillis()
        return Time(timeInMillis)
    }

    override fun currentDate(): Date {
        return Date()
    }
}
