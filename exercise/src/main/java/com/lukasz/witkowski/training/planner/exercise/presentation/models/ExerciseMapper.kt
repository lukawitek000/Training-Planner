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
            image = ImageMapper.toImage(exercise.image)
        )
    }

    fun toPresentationExercise(exercise: DomainExercise): Exercise {
        return Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toCategory(exercise.category),
            image = ImageMapper.toImage(exercise.image)
        )
    }

    fun toPresentationExercises(exercises: List<DomainExercise>): List<Exercise> {
        return exercises.map { exercise -> ExerciseMapper.toPresentationExercise(exercise) }
    }
}