package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference

internal interface ImageReferenceRepository {
    /**
     * @return [ImageId] of the saved [ImageReference], _null_ if saving failed.
     */
    suspend fun save(imageReference: ImageReference): ImageId?

    fun delete(imageReference: ImageReference): Boolean
    /**
    * @return [ImageId] of the updated [ImageReference], _null_ if updating failed.
    */
    fun update(newImageReference: ImageReference, oldImageReference: ImageReference): ImageId?

    suspend fun readByOwnerId(ownerId: String): ImageReference?
    suspend fun read(imageId: ImageId): ImageReference?

}
