package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

object ExerciseMapper {

    fun toDomainExercise(
        exercise: Exercise,
        imageReference: ImageReference? = null
    ): DomainExercise {
        return DomainExercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toExerciseCategory(exercise.category),
            imageId = imageReference?.imageId
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
}
