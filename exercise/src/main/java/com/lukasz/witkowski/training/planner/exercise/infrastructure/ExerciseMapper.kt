package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ImageFile
import com.lukasz.witkowski.training.planner.exercise.domain.ImageId
import com.lukasz.witkowski.training.planner.exercise.domain.ImageReference

internal object ExerciseMapper {

    fun toExerciseWithImage(exercise: Exercise): ExerciseWithImage {
        val dbExercise = DbExercise(
            id = exercise.id.value,
            name = exercise.name,
            description = exercise.description,
            categoryId = exercise.category.ordinal
        )
        val imageReference = exercise.imageReference
        val dbImageReference = imageReference?.run {
            DbImageReference(id.value, dbExercise.id, path)
        }
        return ExerciseWithImage(dbExercise, dbImageReference)
    }

    fun toExercise(exerciseWithImage: ExerciseWithImage): Exercise {
        val (dbExercise, dbImageReference) = exerciseWithImage
        return Exercise(
            id = ExerciseId(dbExercise.id),
            name = dbExercise.name,
            description = dbExercise.description,
            category = ExerciseCategory.values()[dbExercise.categoryId],
            imageReference = dbImageReference?.toImageReference()
        )
    }

    private fun DbImageReference.toImageReference(): ImageReference {
        return ImageFile(ImageId(id), path) // TODO how to handle other types
    }
}