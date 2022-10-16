package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.presentation.ImageId

internal data class ImageByteArray(
    val imageId: ImageId,
    val ownerId: String,
    val data: ByteArray
)