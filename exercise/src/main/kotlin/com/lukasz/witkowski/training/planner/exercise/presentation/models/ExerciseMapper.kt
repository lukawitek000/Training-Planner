package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageMapper
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

object ExerciseMapper {

    fun toDomainExercise(
        exercise: Exercise
    ): DomainExercise {
        return DomainExercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toExerciseCategory(exercise.category),
            imageId = exercise.image?.imageId
        )
    }

    fun toPresentationExercise(
        exercise: DomainExercise,
        imageReference: ImageReference?
    ): Exercise {
        return Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toCategory(exercise.category),
            image = imageReference
        )
    }

    fun toExerciseConfiguration(
        exercise: Exercise,
        image: Image?
    ): ExerciseConfiguration {
        return ExerciseConfiguration(
            exercise.name,
            exercise.description,
            CategoryMapper.toExerciseCategory(exercise.category),
            image?.let { ImageMapper.toImageByteArray(it) }
        )
    }
}
