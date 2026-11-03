package com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.shared.time.Time
import java.util.Date

class FixedTimeProvider(var time: Time = Time.ZERO, var date: Date = Date(0L)) : TimeProvider {
    override fun currentTime(): Time = time

    override fun currentDate(): Date = date
}
