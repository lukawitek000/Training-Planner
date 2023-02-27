package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false
) {

    fun hasCategories(categories: List<ExerciseCategory>): Boolean {
        return getAllCategories().containsAll(categories)
    }

    private fun getAllCategories(): List<ExerciseCategory> {
        return exercises.map { trainingExercise -> trainingExercise.exercise.category }
            .filter { category -> !category.isNone() }
    }
}
