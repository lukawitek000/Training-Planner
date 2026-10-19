package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "ExerciseMutation")
data class DbExerciseMutation(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val idempotencyKey: String,
    val expectedVersion: Long,
    val name: String,
    val description: String,
    val categoryNames: String,
    val state: String,
    val type: String = TYPE_UPDATE,
    val beginnerSets: Int? = null,
    val beginnerReps: Int? = null,
    val beginnerRestTime: Long? = null,
    val beginnerWeight: Int? = null,
    val intermediateSets: Int? = null,
    val intermediateReps: Int? = null,
    val intermediateRestTime: Long? = null,
    val intermediateWeight: Int? = null,
    val advancedSets: Int? = null,
    val advancedReps: Int? = null,
    val advancedRestTime: Long? = null,
    val advancedWeight: Int? = null,
    val conflictServerJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_CREATE = "CREATE"
        const val TYPE_UPDATE = "UPDATE"
        const val TYPE_DELETE = "DELETE"

        const val STATE_PENDING = "PENDING"
        const val STATE_IN_PROGRESS = "IN_PROGRESS"
        const val STATE_CONFLICT = "CONFLICT"
        const val STATE_ERROR = "ERROR"
    }
}
