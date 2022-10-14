package com.lukasz.witkowski.training.planner.training.presentation.models

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false
) {
    fun getCategories(): List<Category> {
        return exercises.map { trainingExercise -> trainingExercise.exercise.category }
            .filter { category -> !category.isNone() }
            .distinct()
    }
}
