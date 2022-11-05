package com.lukasz.witkowski.training.planner.image

data class Image(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val data: ByteArray
) {
    val imageName: String
        get() = imageId.value + "_img"
}
