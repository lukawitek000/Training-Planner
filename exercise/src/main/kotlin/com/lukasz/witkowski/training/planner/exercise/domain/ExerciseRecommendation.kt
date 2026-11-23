package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.shared.utils.Weight
import kotlin.time.Duration

data class ExerciseRecommendation(
    val sets: Int,
    val reps: Int,
    val restTime: Duration,
    val weight: Weight?,
    val time: Duration? = null
)
