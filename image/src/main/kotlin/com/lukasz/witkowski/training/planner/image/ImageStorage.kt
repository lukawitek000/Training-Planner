package com.lukasz.witkowski.training.planner.image

interface ImageStorage {
    fun saveImage(image: ImageByteArray): ImageReference
    fun readImage(imageId: ImageId): ImageByteArray
    fun readImageReference(imageId: ImageId): ImageReference
    fun updateImage(image: ImageByteArray): ImageReference

    /**
     * Delete image which is used by the owner based on the [ownerId].
     * If the image is used by more owners, only the reference to the owner is deleted.
     */
    fun deleteImage(imageId: ImageId, ownerId: String)
}
