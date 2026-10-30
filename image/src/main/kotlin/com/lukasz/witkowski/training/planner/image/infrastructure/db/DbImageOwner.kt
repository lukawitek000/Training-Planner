package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room3.Entity

@Entity(primaryKeys = ["ownerId", "imageId"])
data class DbImageOwner(val ownerId: String, val imageId: String)
