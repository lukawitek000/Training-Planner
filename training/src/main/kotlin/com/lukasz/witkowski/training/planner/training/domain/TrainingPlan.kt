package com.lukasz.witkowski.training.planner.training.domain

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<ExerciseSnapshot>,
    val isSynchronized: Boolean = false,
) {
    fun hasCategories(categories: List<ExerciseCategoryName>): Boolean =
        getAllCategories().containsAll(categories)

    private fun getAllCategories(): List<ExerciseCategoryName> =
        exercises
            .flatMap { trainingExercise -> trainingExercise.categories }
}
