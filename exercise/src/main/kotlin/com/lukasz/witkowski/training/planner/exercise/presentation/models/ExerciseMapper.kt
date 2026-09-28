package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails as DomainExerciseDetails
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

fun DomainExerciseDetails.toPresentationExerciseDetails(): ExerciseDetails =
    ExerciseDetails(
        exercise = this.exercise.toPresentationExercise2(null),
        recommendations = listOf(
            beginnerRecommendation.toRecommendation(RecommendationLevel.BEGINNER),
            intermediateRecommendation.toRecommendation(RecommendationLevel.INTERMEDIATE),
            advancedRecommendation.toRecommendation(RecommendationLevel.ADVANCED),
        )
    )

fun ExerciseRecommendation.toRecommendation(level: RecommendationLevel): Recommendation =
    Recommendation(
        level = level,
        parameters = RecommendedParameters(
            sets = sets,
            reps = reps,
            restTime = restTime,
            weightInKg = weightInKg
        )
    )

fun List<Recommendation>.toExerciseRecommendation(level: RecommendationLevel) =
    first { it.level == level }.toDomainExerciseRecommendation()

private fun Recommendation.toDomainExerciseRecommendation(): ExerciseRecommendation =
    ExerciseRecommendation(
        sets = checkNotNull(parameters.sets) { "Sets cannot be empty" },
        reps = checkNotNull(parameters.reps) { "Reps cannot be empty" },
        restTime = checkNotNull(parameters.restTime) { "Rest time cannot be empty" },
        weightInKg = parameters.weightInKg,
    )

