package com.lukasz.witkowski.training.planner.shared.time

import android.os.SystemClock
import kotlin.time.Clock
import kotlin.time.Instant

class SystemTimeProvider : TimeProvider {
    override fun currentInstant(): Instant = Clock.System.now()

    override fun elapsedTimeMillis(): Long = SystemClock.elapsedRealtime()
}
