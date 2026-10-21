package com.lukasz.witkowski.training.planner.image

data class ImageReference(
    val imageId: ImageId,
    val path: String,
    val isRemote: Boolean = false
)
