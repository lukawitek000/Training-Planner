package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails as DomainExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageReference

fun DomainExercise.toPresentationExercise(images: List<ImageReference> = emptyList()): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        categories = categories.toList(),
        images = images,
        metadata = metadata
    )

fun DomainExerciseDetails.toPresentationExerciseDetails(
    images: List<ImageReference> = emptyList()
): ExerciseDetails = ExerciseDetails(
    exercise = this.exercise.toPresentationExercise(images),
    recommendations =
    listOf(
        beginnerRecommendation.toRecommendation(RecommendationLevel.BEGINNER),
        intermediateRecommendation.toRecommendation(RecommendationLevel.INTERMEDIATE),
        advancedRecommendation.toRecommendation(RecommendationLevel.ADVANCED)
    )
)

fun ExerciseRecommendation.toRecommendation(level: RecommendationLevel): Recommendation =
    Recommendation(
        level = level,
        parameters =
        RecommendedParameters(
            sets = sets,
            reps = reps,
            restTime = restTime,
            weightInKg = weightInKg,
            time = time
        )
    )

fun List<Recommendation>.toExerciseRecommendation(level: RecommendationLevel) = first {
    it.level == level
}.toDomainExerciseRecommendation()

private fun Recommendation.toDomainExerciseRecommendation(): ExerciseRecommendation =
    ExerciseRecommendation(
        sets = checkNotNull(parameters.sets) { "Sets cannot be empty" },
        reps = checkNotNull(parameters.reps) { "Reps cannot be empty" },
        restTime = checkNotNull(parameters.restTime) { "Rest time cannot be empty" },
        weightInKg = parameters.weightInKg,
        time = parameters.time
    )
