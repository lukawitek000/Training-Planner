package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import java.util.Date

class FixedTimeProvider(var time: Time = Time.NONE, var date: Date = Date(0L)) : TimeProvider {
    override fun currentTime(): Time = time
    override fun currentDate(): Date = date
}
