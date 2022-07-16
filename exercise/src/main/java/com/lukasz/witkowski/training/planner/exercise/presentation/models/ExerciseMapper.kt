package com.lukasz.witkowski.training.planner.exercise.presentation.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

object ExerciseMapper {

    fun toDomainExercise(exercise: Exercise): DomainExercise {
        return DomainExercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toExerciseCategory(exercise.category),
            imageId = exercise.imageId
        )
    }

    fun toPresentationExercise(exercise: DomainExercise): Exercise {
        return Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toCategory(exercise.category),
            imageId = exercise.imageId
        )
    }

    fun toPresentationExercises(exercises: List<DomainExercise>): List<Exercise> {
        return exercises.map { exercise -> ExerciseMapper.toPresentationExercise(exercise) }
    }

    private fun Image.toBitmap() = BitmapFactory.decodeByteArray(data, 0, data.size)

//    private fun Bitmap.toImage() = ImageFactory.fromBitmap(bitmap = this)
}