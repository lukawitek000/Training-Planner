package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId

internal data class ImageReference(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val path: String,
    val checksum: Long
)
