package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.presentation.ImageId
import com.lukasz.witkowski.training.planner.image.presentation.ImageReference

internal interface ImageReferenceRepository {
    /**
     * @return [ImageId] of the saved [ImageReference], _null_ if saving failed.
     */
    fun save(imageReference: ImageReference): ImageId?

    fun delete(imageReference: ImageReference): Boolean
    /**
    * @return [ImageId] of the updated [ImageReference], _null_ if updating failed.
    */
    fun update(newImageReference: ImageReference, oldImageReference: ImageReference): ImageId?

    fun read(ownerId: String): ImageReference
}
