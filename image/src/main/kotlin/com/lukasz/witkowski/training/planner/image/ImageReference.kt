package com.lukasz.witkowski.training.planner.image

data class ImageReference(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val path: String
)
