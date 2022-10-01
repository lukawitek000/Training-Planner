package com.lukasz.witkowski.training.planner.exercise.domain

/**
 * Compressed image to reduce its size
 */
class Image(
    val id: ImageId,
    val data: ByteArray? = null,
    val path: String? = null
) {
    val fileName: String
        get() = id.value
}
