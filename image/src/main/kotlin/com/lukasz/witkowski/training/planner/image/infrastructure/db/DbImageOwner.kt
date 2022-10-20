package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DbImageOwner(
    @PrimaryKey
    val ownerId: String,
    val imageId: String
)
