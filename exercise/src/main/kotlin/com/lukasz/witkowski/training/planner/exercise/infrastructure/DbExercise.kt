package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Exercise")
class DbExercise(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val description: String,
    val imageId: String?,
    val ownerId: String? = null,
    val originType: String = ORIGIN_TYPE_BUNDLED
) {
    companion object {
        const val ORIGIN_TYPE_BUNDLED = "BUNDLED"
        const val ORIGIN_TYPE_COMMUNITY = "COMMUNITY"
        const val ORIGIN_TYPE_USER_CREATED = "USER_CREATED"
    }
}
