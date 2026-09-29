package com.lukasz.witkowski.training.planner.exercise.domain

import kotlin.time.Duration

data class ExerciseRecommendation(
    val sets: Int,
    val reps: Int,
    val restTime: Duration,
    val weightInKg: Int?,
)
