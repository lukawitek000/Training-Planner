package com.lukasz.witkowski.training.planner.image

import java.util.UUID

interface ImageStorage {

    suspend fun saveImage(imageConfiguration: ImageConfiguration): ImageReference

    /**
     * Read the image based on the [imageId].
     * @throws [ImageNotFoundException] if the image was not found.
     */
    suspend fun readImage(imageId: ImageId): Image

    /**
     * @return [ImageReference] for the [imageId] or _null_ if it does not exist.
     */
    suspend fun readImageReference(imageId: ImageId): ImageReference?

    /**
     * The update will be done for the owner specified in [newImageConfiguration].
     * @param [newImageConfiguration] the image data to update.
     * @param [imageId] the id of the image that will be updated.
     */
    suspend fun updateImage(imageId: ImageId, newImageConfiguration: ImageConfiguration): ImageReference

    /**
     * Delete image which is used by the owner based on the [ownerId].
     * If the image is used by more owners, only the reference to the owner is deleted.
     */
    suspend fun deleteImage(imageId: ImageId, ownerId: UUID): Boolean
}
