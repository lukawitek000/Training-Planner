package com.lukasz.witkowski.training.planner.session.domain.session.models

import kotlin.time.Duration

data class SessionExerciseConfiguration(
    val reps: Int,
    val weightInKg: Double? = null,
    val holdTime: Duration? = null
) {
    val isIsometric: Boolean
        get() = holdTime != null && holdTime > Duration.ZERO
}
