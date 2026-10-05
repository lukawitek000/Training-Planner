package com.lukasz.witkowski.training.planner.shared.time

import kotlin.time.Instant

interface TimeProvider {
    fun currentInstant(): Instant
}