package com.lukasz.witkowski.training.planner.training.domain

data class TrainingQuery(
    val searchQuery: String,
    val selectedCategories: Set<ExerciseCategoryName>,
    val sortBy: TrainingSortBy
) {
    companion object {
        val DEFAULT = TrainingQuery(
            "", emptySet(),
            TrainingSortBy.Used(SortDirection.ASCENDING)
        )
    }
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

sealed interface TrainingSortBy {
    val sortDirection: SortDirection

    data class Used(override val sortDirection: SortDirection) : TrainingSortBy
    data class Modified(override val sortDirection: SortDirection) : TrainingSortBy
}