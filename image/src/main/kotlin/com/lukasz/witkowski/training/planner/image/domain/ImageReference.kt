package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId
import java.util.UUID

internal data class ImageReference(
    val imageId: ImageId,
    val ownersIds: List<UUID>,
    val path: String,
    val checksum: Long
)
