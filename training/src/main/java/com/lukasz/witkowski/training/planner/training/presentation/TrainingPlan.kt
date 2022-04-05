package com.lukasz.witkowski.training.planner.training.presentation

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false // TODO check if it is needed in presentation layer
) {
    fun getAllCategories(): List<Category> {
        return exercises.map { exercise -> exercise.category }
            .filter { category -> !category.isNone() }
            .distinct()
    }
}