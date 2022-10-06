package com.lukasz.witkowski.training.planner.exercise.domain

/**
 * Compressed image to reduce its size
 */
//class ImageWithData(
//    val id: ImageId,
//    val data: ByteArray
//) {
//    val fileName: String
//        get() = id.value
//}
//
//class Image(
//    val id: ImageId,
//    val path: String
//)


sealed interface Image {
    val id: ImageId
    val imageName: String
        get() = "${id.value}_image"
}

sealed interface ImageReference: Image {
    /**
     * Path to the location where the image is stored without name of the image
     */
    val path: String
}

data class ImageByteArray(
    override val id: ImageId = ImageId.create(),
    val data: ByteArray
): Image

data class ImageFile(
    override val id: ImageId,
    override val path: String
): ImageReference