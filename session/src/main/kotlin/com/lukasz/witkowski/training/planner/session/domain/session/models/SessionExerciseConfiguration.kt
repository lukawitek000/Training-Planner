package com.lukasz.witkowski.training.planner.session.domain.session.models

import com.lukasz.witkowski.training.planner.shared.utils.Weight
import kotlin.time.Duration

data class SessionExerciseConfiguration(
    val reps: Int,
    val weight: Weight? = null,
    val holdTime: Duration? = null
) {
    val isIsometric: Boolean
        get() = holdTime != null && holdTime > Duration.ZERO
}
