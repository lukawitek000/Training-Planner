package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

interface TimeProvider {
    /**
     * Current wall-clock instant (UTC). Use for timestamps and calendar display, never for
     * measuring elapsed intervals.
     */
    fun currentInstant(): Instant

    /**
     * Monotonic milliseconds elapsed since device boot (continues ticking during CPU deep sleep).
     */
    fun elapsedTimeMillis(): Long

    /**
     * Monotonic duration elapsed since device boot.
     */
    fun elapsedTime(): Duration = elapsedTimeMillis().milliseconds
}
