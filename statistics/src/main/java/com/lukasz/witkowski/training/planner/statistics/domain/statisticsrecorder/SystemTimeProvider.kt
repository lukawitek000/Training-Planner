package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.TimeProvider

class SystemTimeProvider: TimeProvider {
    override fun currentTime(): Time {
        val timeInMillis = System.currentTimeMillis()
        return Time(timeInMillis)
    }
}