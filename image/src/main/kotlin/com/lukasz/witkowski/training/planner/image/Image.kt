package com.lukasz.witkowski.training.planner.image

data class Image internal constructor(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val data: ByteArray,
    val checksum: Long
) {
    val imageName: String
        get() = imageId.value + "_img"
}
