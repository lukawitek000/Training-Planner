package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time

class FixedTimeProvider(var time: Time = Time.NONE) : TimeProvider {
    override fun currentTime(): Time = time
}
