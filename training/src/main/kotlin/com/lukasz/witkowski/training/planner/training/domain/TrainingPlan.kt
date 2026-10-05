package com.lukasz.witkowski.training.planner.training.domain

import kotlin.time.Duration
import kotlin.time.Instant

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val restTime: Duration,
    val lastModification: Instant,
    val lastSession: Instant?,
) {
    val categories = exercises
        .flatMap { trainingExercise -> trainingExercise.exercise.categories }
        .toSet()
}
