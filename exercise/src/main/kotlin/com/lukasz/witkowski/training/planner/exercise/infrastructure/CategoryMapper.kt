package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

internal fun DbExerciseCategory.toExerciseCategory(): ExerciseCategory =
    ExerciseCategory(name = categoryName)

internal fun ExerciseCategory.toDbExerciseCategory(): DbExerciseCategory = DbExerciseCategory(
    categoryName = this.name
)
