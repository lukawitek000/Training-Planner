package com.lukasz.witkowski.training.planner.training.domain

import kotlin.time.Duration
import kotlin.time.Instant

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<ExerciseSnapshot>,
    val restTime: Duration,
    val lastModification: Instant,
    val lastSession: Instant?,
) {
    fun hasCategories(categories: List<ExerciseCategoryName>): Boolean =
        getAllCategories().containsAll(categories)

    private fun getAllCategories(): List<ExerciseCategoryName> =
        exercises
            .flatMap { trainingExercise -> trainingExercise.categories }
}
