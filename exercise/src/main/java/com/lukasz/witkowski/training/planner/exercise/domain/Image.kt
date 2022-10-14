package com.lukasz.witkowski.training.planner.exercise.domain

sealed interface Image {
    val id: ImageId
    val imageName: String
        get() = "${id.value}_image"
}

data class ImageByteArray(
    override val id: ImageId = ImageId.create(),
    val data: ByteArray
): Image

data class ImageReference(
    override val id: ImageId,
    val path: String,
    val source: ImageSource = ImageSource.FILE
): Image {
    /**
     * Complete path to the image file.
     */
    val absolutePath: String
        get() = "$path/$imageName"

    enum class ImageSource {
        FILE
    }
}