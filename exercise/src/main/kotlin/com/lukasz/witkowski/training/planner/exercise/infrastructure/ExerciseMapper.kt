package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal fun Exercise.toDbExercise(): DbExercise =
    DbExercise(
        exerciseId = id.toString(),
        name = name,
        description = description,
        imageId = imageId?.toString(),
    )

internal fun Exercise.toDbExerciseWithCategories(): DbExerciseWithCategories {
    return DbExerciseWithCategories(
        exercise = this.toDbExercise(),
        categories = categories.map { it.toDbExerciseCategory() }
    )
}



internal fun DbExerciseWithCategories.toExercise(): Exercise =
    Exercise(
        id = ExerciseId(exercise.exerciseId),
        name = exercise.name,
        description = exercise.description,
        categories = categories.map { it.toExerciseCategory() },
        imageId = exercise.imageId?.let { ImageId(it) },
    )

