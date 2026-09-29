package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false,
) {
    fun hasCategories(categories: List<ExerciseCategoryLegacy>): Boolean = getAllCategories().containsAll(categories)

    private fun getAllCategories(): List<ExerciseCategoryLegacy> =
        exercises
            .map { trainingExercise -> trainingExercise.exercise.categories.first() }
            .filter { category -> !category.isNone() }
}
