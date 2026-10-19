package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

data class DbExerciseOwner(
    val ownerId: String,
    val ownerName: String
)

@Entity(tableName = "Exercise")
data class DbExercise(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val description: String,
    val imageId: String?,
    @Embedded
    val owner: DbExerciseOwner? = null,
    val sourceType: String = SOURCE_TYPE_BUNDLED,
    val sharing: String = SHARING_PRIVATE,
    val revision: Long = 1L,
    val syncState: String = SYNC_STATE_SYNCED
) {
    companion object {
        const val SOURCE_TYPE_BUNDLED = "BUNDLED"
        const val SOURCE_TYPE_USER_CREATED = "USER_CREATED"
        const val SOURCE_TYPE_ORGANIZATION = "ORGANIZATION"

        const val SHARING_PRIVATE = "PRIVATE"
        const val SHARING_COMMUNITY = "COMMUNITY"
        const val SHARING_FRIENDS = "FRIENDS"

        const val SYNC_STATE_LOCAL_ONLY = "LOCAL_ONLY"
        const val SYNC_STATE_PENDING_UPLOAD = "PENDING_UPLOAD"
        const val SYNC_STATE_SYNCED = "SYNCED"
        const val SYNC_STATE_PENDING_DELETE = "PENDING_DELETE"
        const val SYNC_STATE_CONFLICT = "CONFLICT"
        const val SYNC_STATE_ERROR = "ERROR"
    }
}
