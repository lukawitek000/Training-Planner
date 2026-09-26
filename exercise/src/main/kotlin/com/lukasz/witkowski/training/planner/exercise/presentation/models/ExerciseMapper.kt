package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2 as DomainExercise2

fun Exercise.toDomainExercise(): DomainExercise =
    DomainExercise(
        id = id,
        name = name,
        description = description,
        categories = categories.map { it.toExerciseCategory() },
        imageId = image?.imageId,
    )

fun DomainExercise.toPresentationExercise(imageReference: ImageReference?): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        categories = categories.map { it.toCategory() },
        image = imageReference,
    )

fun DomainExercise2.toPresentationExercise2(imageReference: ImageReference?): Exercise2 =
    Exercise2(
        id = id,
        name = name,
        description = description,
        categories = categories,
        image = imageReference,
    )
