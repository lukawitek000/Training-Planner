package com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import java.util.Date
import kotlin.time.Duration

class FixedTimeProvider(var time: Duration = Duration.ZERO, var date: Date = Date(0L)) : TimeProvider {
    override fun currentTime(): Long = time.inWholeMilliseconds

    override fun currentDate(): Date = date

    override fun elapsedTimeMillis(): Long = time.inWholeMilliseconds

    fun advanceBy(duration: Duration) {
        time += duration
    }
}
