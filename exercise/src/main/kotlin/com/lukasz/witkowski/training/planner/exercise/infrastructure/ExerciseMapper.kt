package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSharing
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.image.ImageId
import kotlin.time.Duration.Companion.seconds

internal fun Exercise2.toDbExercise(): DbExercise {
    val (sourceTypeStr, dbOwner) = when (val s = metadata.source) {
        is ExerciseSource.Bundled -> DbExercise.SOURCE_TYPE_BUNDLED to null
        is ExerciseSource.Organization -> DbExercise.SOURCE_TYPE_ORGANIZATION to null
        is ExerciseSource.UserCreated -> {
            val owner = when (val o = s.owner) {
                is ExerciseOwner.Local -> DbExerciseOwner(o.id, "Local")
                is ExerciseOwner.User -> DbExerciseOwner(o.id, o.username)
            }
            DbExercise.SOURCE_TYPE_USER_CREATED to owner
        }
    }
    val sharingStr = when (metadata.sharing) {
        is ExerciseSharing.Private -> DbExercise.SHARING_PRIVATE
        is ExerciseSharing.Community -> DbExercise.SHARING_COMMUNITY
        is ExerciseSharing.Friends -> DbExercise.SHARING_FRIENDS
    }
    val syncStateStr = metadata.syncState.name

    return DbExercise(
        exerciseId = id.toString(),
        name = name,
        description = description,
        imageId = imageId?.toString(),
        owner = dbOwner,
        sourceType = sourceTypeStr,
        sharing = sharingStr,
        revision = metadata.revision,
        syncState = syncStateStr
    )
}

internal fun DbExercise.toExercise(categories: List<ExerciseCategory>): Exercise2 {
    val sourceObj = toExerciseSource()
    val sharingObj = toExerciseSharing()
    val syncStateObj = toExerciseSyncState()
    val metadataObj = ExerciseMetadata(
        source = sourceObj,
        sharing = sharingObj,
        revision = revision,
        syncState = syncStateObj
    )
    val categoriesSet = categories.toSet().ifEmpty { setOf(ExerciseCategory("Other")) }
    return Exercise2(
        id = ExerciseId(exerciseId),
        name = name,
        description = description,
        categories = categoriesSet,
        imageId = imageId?.let { ImageId(it) },
        metadata = metadataObj
    )
}

internal fun DbExerciseWithCategories.toExercise(): Exercise2 =
    exercise.toExercise(categories.map { it.toExerciseCategory() })

private fun DbExercise.toExerciseSource(): ExerciseSource = when (sourceType) {
    DbExercise.SOURCE_TYPE_USER_CREATED -> {
        val ownerObj = if (owner == null || owner.ownerName.isBlank() || owner.ownerName == "Local") {
            ExerciseOwner.Local(id = owner?.ownerId ?: "local_user")
        } else {
            ExerciseOwner.User(id = owner.ownerId, username = owner.ownerName)
        }
        ExerciseSource.UserCreated(ownerObj)
    }
    DbExercise.SOURCE_TYPE_ORGANIZATION -> ExerciseSource.Organization
    else -> ExerciseSource.Bundled
}

private fun DbExercise.toExerciseSharing(): ExerciseSharing = when (sharing) {
    DbExercise.SHARING_COMMUNITY -> ExerciseSharing.Community
    DbExercise.SHARING_FRIENDS -> ExerciseSharing.Friends()
    else -> ExerciseSharing.Private
}

private fun DbExercise.toExerciseSyncState(): ExerciseSyncState = try {
    ExerciseSyncState.valueOf(syncState)
} catch (_: Exception) {
    ExerciseSyncState.SYNCED
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
    weightInKg = weightInKg,
    timeInSeconds = time?.inWholeSeconds
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
): ExerciseRecommendation = firstOrNull {
    it.level == level
}?.toExerciseRecommendation() ?: ExerciseRecommendation(
    sets = 0,
    reps = 0,
    restTime = 0.seconds,
    weightInKg = null,
    time = null
)

private fun DbExerciseRecommendation.toExerciseRecommendation(): ExerciseRecommendation =
    ExerciseRecommendation(
        sets = sets,
        reps = reps,
        restTime = restTimeInSeconds.seconds,
        weightInKg = weightInKg,
        time = timeInSeconds?.seconds
    )
