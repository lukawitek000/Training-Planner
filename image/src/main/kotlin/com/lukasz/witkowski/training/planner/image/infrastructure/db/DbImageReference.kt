package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class DbImageReference(
    @PrimaryKey
    val id: String,
    val path: String,
    val checksum: Long,
)
