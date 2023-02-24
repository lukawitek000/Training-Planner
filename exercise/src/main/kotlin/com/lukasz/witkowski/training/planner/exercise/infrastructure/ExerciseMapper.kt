package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal fun Exercise.toDbExercise(): DbExercise {
    return DbExercise(
        id = id.toString(),
        name = name,
        description = description,
        categoryId = category.ordinal,
        imageId = imageId?.toString()
    )
}

internal fun DbExercise.toExercise(): Exercise {
    return Exercise(
        id = ExerciseId(id),
        name = name,
        description = description,
        category = ExerciseCategory.values()[categoryId],
        imageId = imageId?.let { ImageId(it) }
    )
}
