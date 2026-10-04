package com.lukasz.witkowski.training.planner.training.domain


@JvmInline
value class ExerciseCategoryName(val name: String) {
    init {
        require(name.isNotEmpty()) {
            "Category name cannot be empty"
        }
    }
}
