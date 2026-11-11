package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import android.os.SystemClock
import java.util.Date

class SystemTimeProvider : TimeProvider {
    override fun currentTime(): Long = System.currentTimeMillis()

    override fun currentDate(): Date = Date()

    override fun elapsedTimeMillis(): Long = SystemClock.elapsedRealtime()
}
