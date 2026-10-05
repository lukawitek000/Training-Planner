package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Instant

class TestTimeProvider: TimeProvider {
    var instant = Instant.parse("2026-09-10T12:00:00Z")
    override fun currentInstant(): Instant {
        return instant
    }
}
