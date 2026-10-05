package com.lukasz.witkowski.training.planner.training.domain

import kotlin.time.Duration
import kotlin.time.Instant

// TODO probably it would be good to have a training plan config
// Then mapping for Room and all the saving, tests should be sufficient,
// Ask agent to verify and add missing combinations
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
