package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DbImageReference(
    @PrimaryKey
    val id: String,
    val exerciseId: String,
    val path: String
)
