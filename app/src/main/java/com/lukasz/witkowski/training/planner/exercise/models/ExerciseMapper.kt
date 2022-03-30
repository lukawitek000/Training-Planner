package com.lukasz.witkowski.training.planner.exercise.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image

internal object ExerciseMapper {


    fun toDomainExercise(exercise: Exercise): com.lukasz.witkowski.training.planner.exercise.domain.Exercise {
        return com.lukasz.witkowski.training.planner.exercise.domain.Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toExerciseCategory(exercise.category),
            image = exercise.image?.toImage()
        )
    }

    private fun toPresentationExercise(exercise: com.lukasz.witkowski.training.planner.exercise.domain.Exercise): Exercise {
        return Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toCategory(exercise.category),
            image = exercise.image?.toBitmap()
        )
    }

    fun toPresentationExercises(exercises: List<com.lukasz.witkowski.training.planner.exercise.domain.Exercise>): List<Exercise> {
        return exercises.map { exercise -> ExerciseMapper.toPresentationExercise(exercise) }
    }

    private fun Image.toBitmap() = BitmapFactory.decodeByteArray(data, 0, data.size)

    private fun Bitmap.toImage() = ImageFactory.fromBitmap(bitmap = this)

}