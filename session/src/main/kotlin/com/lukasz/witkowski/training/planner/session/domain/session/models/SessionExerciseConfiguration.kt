package com.lukasz.witkowski.training.planner.session.domain.session.models

import kotlin.time.Duration

data class SessionExerciseConfiguration(
    val reps: Int,
    val weightInKg: Double?,
    val holdTime: Duration?
) {
    val isIsometric = holdTime != null
}
