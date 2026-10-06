package com.lukasz.witkowski.training.planner.training.domain

data class TrainingQuery(
    val searchQuery: String,
    val selectedCategories: Set<ExerciseCategoryName>,
) {
    companion object {
        val DEFAULT =
            TrainingQuery(
                "",
                emptySet(),
            )
    }
}
