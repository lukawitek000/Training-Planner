package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId

internal interface ImageReferenceRepository {
    /**
     * @return [ImageId] of the saved [ImageReference], _null_ if saving failed.
     */
    suspend fun save(imageReference: ImageReference): ImageId?

    /**
     * Deletes image reference for all of owners included in the [imageReference].
     * @return _true_ if delete was successful, otherwise _false_.
     */
    suspend fun delete(imageReference: ImageReference): Boolean
    /**
    * @return [ImageId] of the updated [ImageReference], _null_ if updating failed.
    */
    suspend fun update(newImageReference: ImageReference, oldImageReference: ImageReference): ImageId?

    suspend fun readByOwnerId(ownerId: String): ImageReference?
    suspend fun read(imageId: ImageId): ImageReference?

    /**
     * Check if the [ownersIds] are all owners of the image identified by [imageId]
     */
    suspend fun areAllImageOwners(imageId: ImageId, ownersIds: List<String>): Boolean

    /**
     * Check if the image with the provided [checksum] is already stored.
     */
    suspend fun isImageAlreadySaved(checksum: Long): Boolean
    suspend fun addOwnerToImage(checksum: Long, ownerId: String): ImageReference
}
