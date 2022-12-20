package com.lukasz.witkowski.training.planner.image

import java.util.UUID

data class ImageConfiguration(
    val data: ByteArray,
    val ownerId: UUID
)
