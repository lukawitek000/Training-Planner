package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails as DomainExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOverview as DomainExerciseOverview
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.image.ImageReference

fun Exercise.toDomainExercise(): DomainExercise = DomainExercise(
    id = id,
    name = name,
    description = description,
    categories = categories.map { it.toExerciseCategory() },
    imageId = image?.imageId
)

fun DomainExercise.toPresentationExercise(imageReference: ImageReference?): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    categories = categories.map { it.toCategory() },
    image = imageReference
)

fun DomainExerciseOverview.toPresentationExerciseOverview(
    thumbnail: ImageReference? = null
): ExerciseOverview = ExerciseOverview(
    id = id,
    name = name,
    description = description,
    categories = categories.toList(),
    thumbnail = thumbnail,
    metadata = metadata
)

@Deprecated(
    message = "Use toPresentationExerciseOverview instead",
    replaceWith = ReplaceWith("toPresentationExerciseOverview(thumbnail)")
)
fun DomainExerciseOverview.toPresentationExercise2(
    thumbnail: ImageReference? = null
): ExerciseOverview = toPresentationExerciseOverview(thumbnail)

fun DomainExerciseDetails.toPresentationExerciseDetails(
    images: List<ImageReference> = emptyList()
): ExerciseDetails = ExerciseDetails(
    exercise = this.exercise.toPresentationExerciseOverview(images.firstOrNull()),
    recommendations =
    listOf(
        beginnerRecommendation.toRecommendation(RecommendationLevel.BEGINNER),
        intermediateRecommendation.toRecommendation(RecommendationLevel.INTERMEDIATE),
        advancedRecommendation.toRecommendation(RecommendationLevel.ADVANCED)
    ),
    images = images
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
