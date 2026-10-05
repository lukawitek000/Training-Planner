package com.lukasz.witkowski.training.planner.training.domain

import kotlin.time.Duration

data class TrainingPlanConfiguration(
    val title: String,
    val description: String,
    val exercises: List<ExerciseSnapshot>,
    val restTime: Duration,
)
