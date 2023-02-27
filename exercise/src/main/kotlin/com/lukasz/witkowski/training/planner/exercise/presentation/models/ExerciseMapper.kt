package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

fun Exercise.toDomainExercise(): DomainExercise {
    return DomainExercise(
        id = id,
        name = name,
        description = description,
        category = category.toExerciseCategory(),
        imageId = image?.imageId
    )
}

fun DomainExercise.toPresentationExercise(imageReference: ImageReference?): Exercise {
    return Exercise(
        id = id,
        name = name,
        description = description,
        category = category.toCategory(),
        image = imageReference
    )
}
