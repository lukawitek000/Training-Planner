package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

internal fun DbExerciseCategory.toExerciseCategory(): ExerciseCategory {
    return ExerciseCategory.entries.first {
        it.name == this.categoryName
    }
}

internal fun ExerciseCategory.toDbExerciseCategory(): DbExerciseCategory {
    return DbExerciseCategory(
        categoryName = this.name
    )
}