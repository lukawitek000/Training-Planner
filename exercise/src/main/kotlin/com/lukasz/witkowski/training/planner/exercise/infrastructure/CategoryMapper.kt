package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy

internal fun DbExerciseCategory.toExerciseCategoryLegacy(): ExerciseCategoryLegacy =
    ExerciseCategoryLegacy.entries.first {
        it.name == this.categoryName
    }

internal fun ExerciseCategoryLegacy.toDbExerciseCategory(): DbExerciseCategory =
    DbExerciseCategory(
        categoryName = this.name,
    )

internal fun DbExerciseCategory.toExerciseCategory(): ExerciseCategory = ExerciseCategory(name = categoryName)

internal fun ExerciseCategory.toDbExerciseCategory(): DbExerciseCategory =
    DbExerciseCategory(
        categoryName = this.name,
    )
