package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageId
import kotlin.time.Duration.Companion.seconds

internal fun Exercise2.toDbExercise(): DbExercise = DbExercise(
    exerciseId = id.toString(),
    name = name,
    description = description,
    imageId = imageId?.toString()
)

internal fun DbExercise.toExercise(
    categories: List<ExerciseCategory>,
    images: List<DbExerciseImage> = emptyList()
): Exercise2 {
    val imageIds = if (images.isNotEmpty()) {
        images.sortedBy { it.orderIndex }.map { ImageId(it.imageId) }
    } else {
        listOfNotNull(imageId?.let { ImageId(it) })
    }
    return Exercise2(
        id = ExerciseId(exerciseId),
        name = name,
        description = description,
        categories = categories.toSet(),
        imageIds = imageIds
    )
}

internal fun DbExerciseWithCategories.toExercise(): Exercise2 {
    val imageIds = if (images.isNotEmpty()) {
        images.sortedBy { it.orderIndex }.map { ImageId(it.imageId) }
    } else {
        listOfNotNull(exercise.imageId?.let { ImageId(it) })
    }
    return Exercise2(
        id = ExerciseId(exercise.exerciseId),
        name = exercise.name,
        description = exercise.description,
        categories = categories.map { it.toExerciseCategory() }.toSet(),
        imageIds = imageIds
    )
}

internal fun ExerciseDetails.toDbExerciseDetails(): DbExerciseDetails = DbExerciseDetails(
    exercise = exercise.toDbExercise(),
    categories = exercise.categories.map { it.toDbExerciseCategory() },
    recommendations =
    listOf(
        beginnerRecommendation.toDbExerciseRecommendation(
            exerciseId = exercise.id,
            level = RecommendationLevel.BEGINNER
        ),
        intermediateRecommendation.toDbExerciseRecommendation(
            exerciseId = exercise.id,
            level = RecommendationLevel.INTERMEDIATE
        ),
        advancedRecommendation.toDbExerciseRecommendation(
            exerciseId = exercise.id,
            level = RecommendationLevel.ADVANCED
        )
    ),
    images = exercise.imageIds.mapIndexed { index, imageId ->
        DbExerciseImage(
            exerciseId = exercise.id.toString(),
            imageId = imageId.toString(),
            orderIndex = index
        )
    }
)

internal fun ExerciseRecommendation.toDbExerciseRecommendation(
    exerciseId: ExerciseId,
    level: RecommendationLevel
): DbExerciseRecommendation = DbExerciseRecommendation(
    exerciseId = exerciseId.toString(),
    level = level,
    sets = sets,
    reps = reps,
    restTimeInSeconds = restTime.inWholeSeconds,
    weightInKg = weightInKg
)

internal fun DbExerciseDetails.toExerciseDetails(): ExerciseDetails {
    val categories = categories.map { it.toExerciseCategory() }
    return ExerciseDetails(
        exercise = exercise.toExercise(categories, images),
        beginnerRecommendation = recommendations.toExerciseRecommendation(
            RecommendationLevel.BEGINNER
        ),
        intermediateRecommendation = recommendations.toExerciseRecommendation(
            RecommendationLevel.INTERMEDIATE
        ),
        advancedRecommendation = recommendations.toExerciseRecommendation(
            RecommendationLevel.ADVANCED
        )
    )
}

private fun List<DbExerciseRecommendation>.toExerciseRecommendation(
    level: RecommendationLevel
): ExerciseRecommendation = first {
    it.level == level
}.toExerciseRecommendation()

private fun DbExerciseRecommendation.toExerciseRecommendation(): ExerciseRecommendation =
    ExerciseRecommendation(
        sets = sets,
        reps = reps,
        restTime = restTimeInSeconds.seconds,
        weightInKg = weightInKg
    )
