package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Clock
import kotlin.time.Instant

class SystemTimeProvider: TimeProvider {
    override fun currentInstant(): Instant {
        return Clock.System.now()
    }
}