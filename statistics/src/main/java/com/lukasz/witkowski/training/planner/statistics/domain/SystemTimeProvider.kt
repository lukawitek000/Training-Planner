package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time

class SystemTimeProvider: TimeProvider {
    override fun currentTime(): Time {
        val timeInMillis = System.currentTimeMillis()
        return Time(timeInMillis)
    }
}