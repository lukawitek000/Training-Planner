package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId

internal class Image(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val data: ByteArray,
    val checksum: Long
) {
    val imageName: String
        get() = imageId.toString() + IMAGE_NAME_SUFFIX

    companion object {
        private const val IMAGE_NAME_SUFFIX = "_img"
    }
}
