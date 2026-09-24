package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

fun Exercise.toDomainExercise(): DomainExercise =
    DomainExercise(
        id = id,
        name = name,
        description = description,
        categories = listOf(category.toExerciseCategory()),
        imageId = image?.imageId,
    )

fun DomainExercise.toPresentationExercise(imageReference: ImageReference?): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        category = categories.first().toCategory(),
        image = imageReference,
    )
