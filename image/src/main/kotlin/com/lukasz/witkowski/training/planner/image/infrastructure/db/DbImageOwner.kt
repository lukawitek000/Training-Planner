package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class DbImageOwner(
    @PrimaryKey
    val ownerId: String,
    val imageId: String,
)
