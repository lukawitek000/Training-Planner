package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.isCategoryNone

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false // TODO how to handle synchronization flags
    // Enum flag created, deleted up-to-date
) {

    fun hasCategories(categories: List<ExerciseCategory>): Boolean {
        return getAllCategories().containsAll(categories)
    }

    private fun getAllCategories(): List<ExerciseCategory> {
        return exercises.map { trainingExercise -> trainingExercise.exercise.category }
            .filter { category -> !isCategoryNone(category) }
    }
}