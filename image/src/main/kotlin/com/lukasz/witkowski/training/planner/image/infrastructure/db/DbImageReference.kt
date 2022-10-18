package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DbImageReference(
    @PrimaryKey
    val id: String,
    val path: String
)
