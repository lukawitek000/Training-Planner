package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import android.os.SystemClock
import com.lukasz.witkowski.training.planner.shared.time.Time
import java.util.Date

class SystemTimeProvider : TimeProvider {
    override fun currentTime(): Time {
        val timeInMillis = System.currentTimeMillis()
        return Time(timeInMillis)
    }

    override fun currentDate(): Date = Date()

    override fun elapsedTimeMillis(): Long = SystemClock.elapsedRealtime()
}
