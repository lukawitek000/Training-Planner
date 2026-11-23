package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Duration
import kotlin.time.Instant

class TestTimeProvider(
    var instant: Instant = Instant.parse("2026-09-10T12:00:00Z"),
    var elapsedMillis: Long = 0L
) : TimeProvider {
    override fun currentInstant(): Instant = instant

    override fun elapsedTimeMillis(): Long = elapsedMillis

    fun advanceBy(duration: Duration) {
        instant += duration
        elapsedMillis += duration.inWholeMilliseconds
    }
}
