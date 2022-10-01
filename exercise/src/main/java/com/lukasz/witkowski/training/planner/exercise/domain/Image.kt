package com.lukasz.witkowski.training.planner.exercise.domain

/**
 * Compressed image to reduce its size
 */
class ImageWithData(
    val id: ImageId,
    val data: ByteArray
) {
    val fileName: String
        get() = id.value
}

class Image(
    val id: ImageId,
    val path: String
)
