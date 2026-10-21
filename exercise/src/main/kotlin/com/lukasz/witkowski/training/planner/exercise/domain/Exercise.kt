package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategoryLegacy>,
    val imageId: ImageId? = null
) {
    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}

data class ExerciseMetadata(
    val source: ExerciseSource = ExerciseSource.Bundled,
    val sharing: ExerciseSharing = ExerciseSharing.Private,
    val revision: Long = 1L,
    val syncState: ExerciseSyncState = ExerciseSyncState.SYNCED
)

data class Exercise2(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: Set<ExerciseCategory>,
    val imageId: ImageId? = null,
    val metadata: ExerciseMetadata = ExerciseMetadata()
) {
    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}

sealed interface ExerciseSource {
    data object Bundled : ExerciseSource
    data class UserCreated(val owner: ExerciseOwner) : ExerciseSource
    data object Organization : ExerciseSource
}

sealed interface ExerciseSharing {
    data object Private : ExerciseSharing
    data object Community : ExerciseSharing
    data class Friends(val selectedUserIds: Set<String> = emptySet()) : ExerciseSharing
}

enum class ExerciseSyncState {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    SYNCED,
    PENDING_DELETE,
    CONFLICT,
    ERROR
}

sealed interface ExerciseOwner {
    val id: String

    data class Local(override val id: String = "local_user") : ExerciseOwner

    data class User(override val id: String, val username: String) : ExerciseOwner
}
