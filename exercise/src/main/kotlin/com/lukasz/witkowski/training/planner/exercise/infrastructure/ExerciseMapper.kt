package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOrigin
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageId
import kotlin.time.Duration.Companion.seconds

internal fun Exercise2.toDbExercise(): DbExercise {
    val (dbOwner, originType) = when (val origin = origin) {
        is ExerciseOrigin.Bundled -> null to DbExercise.ORIGIN_TYPE_BUNDLED
        is ExerciseOrigin.Community -> {
            val dbOwner = when (val owner = origin.owner) {
                is ExerciseOwner.Local -> DbExerciseOwner(owner.id, "Local")
                is ExerciseOwner.User -> DbExerciseOwner(owner.id, owner.username)
            }
            dbOwner to DbExercise.ORIGIN_TYPE_COMMUNITY
        }
        is ExerciseOrigin.UserCreated -> {
            val dbOwner = when (val owner = origin.owner) {
                is ExerciseOwner.Local -> DbExerciseOwner(owner.id, "Local")
                is ExerciseOwner.User -> DbExerciseOwner(owner.id, owner.username)
            }
            dbOwner to DbExercise.ORIGIN_TYPE_USER_CREATED
        }
    }
    return DbExercise(
        exerciseId = id.toString(),
        name = name,
        description = description,
        imageId = imageId?.toString(),
        owner = dbOwner,
        originType = originType
    )
}

internal fun DbExercise.toExercise(categories: List<ExerciseCategory>): Exercise2 {
    val origin = toExerciseOrigin()
    return Exercise2(
        id = ExerciseId(exerciseId),
        name = name,
        description = description,
        categories = categories.toSet(),
        imageId = imageId?.let { ImageId(it) },
        origin = origin
    )
}

internal fun DbExerciseWithCategories.toExercise(): Exercise2 {
    val origin = exercise.toExerciseOrigin()
    return Exercise2(
        id = ExerciseId(exercise.exerciseId),
        name = exercise.name,
        description = exercise.description,
        categories = categories.map { it.toExerciseCategory() }.toSet(),
        imageId = exercise.imageId?.let { ImageId(it) },
        origin = origin
    )
}

private fun DbExercise.toExerciseOrigin(): ExerciseOrigin = when (originType) {
    DbExercise.ORIGIN_TYPE_COMMUNITY -> ExerciseOrigin.Community(
        owner = if (owner == null || owner.ownerName.isBlank() || owner.ownerName == "Local") {
            ExerciseOwner.Local(id = owner?.ownerId ?: "local_user")
        } else {
            ExerciseOwner.User(id = owner.ownerId, username = owner.ownerName)
        }
    )
    DbExercise.ORIGIN_TYPE_USER_CREATED -> ExerciseOrigin.UserCreated(
        owner = if (owner == null || owner.ownerName.isBlank() || owner.ownerName == "Local") {
            ExerciseOwner.Local(id = owner?.ownerId ?: "local_user")
        } else {
            ExerciseOwner.User(id = owner.ownerId, username = owner.ownerName)
        }
    )
    else -> ExerciseOrigin.Bundled
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
    )
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
        exercise = exercise.toExercise(categories),
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
