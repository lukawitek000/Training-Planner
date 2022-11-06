package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId

internal class Image(
    val imageId: ImageId,
    val ownersIds: List<String>, // no need to expose it
    val data: ByteArray,
    val checksum: Long // no need to expose it
) {
    val imageName: String
        get() = imageId.value + "_img" // no need to expose it
}
