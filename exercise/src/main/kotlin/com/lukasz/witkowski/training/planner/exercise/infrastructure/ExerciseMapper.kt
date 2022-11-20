package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal object ExerciseMapper {

    fun toDbExercise(exercise: Exercise): DbExercise {
        return DbExercise(
            id = exercise.id.value.toString(),
            name = exercise.name,
            description = exercise.description,
            categoryId = exercise.category.ordinal,
            imageId = exercise.imageId?.value
        )
    }

    fun toExercise(dbExercise: DbExercise): Exercise {
        return Exercise(
            id = ExerciseId(dbExercise.id),
            name = dbExercise.name,
            description = dbExercise.description,
            category = ExerciseCategory.values()[dbExercise.categoryId],
            imageId = dbExercise.imageId?.let { ImageId(it) }
        )
    }
}
