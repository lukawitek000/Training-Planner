package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId
import java.util.UUID

internal class Image(
    val imageId: ImageId,
    val ownersIds: List<UUID>,
    val data: ByteArray,
    val checksum: Long
) {
    val imageName: String
        get() = imageId.toString() + IMAGE_NAME_SUFFIX

    companion object {
        private const val IMAGE_NAME_SUFFIX = "_img"
    }
}
