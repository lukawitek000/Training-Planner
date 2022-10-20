package com.lukasz.witkowski.training.planner.image.domain

import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageReference
import java.io.IOException

internal interface ImageRepository {
    /**
     * Save [ImageByteArray].
     * @throws [Exception] if saving the [ImageByteArray] fails.
     * @return [ImageReference] with path to the image.
     */
    suspend fun save(image: ImageByteArray): ImageReference

    /**
     * Deletes image which was saved under the provided name.
     * @return _true_ if the file was deleted and _false_ otherwise.
     */
    suspend fun delete(imageReference: ImageReference): Boolean

    /**
     * Update file with new [ImageByteArray].
     * @throws [Exception] if updating the [ImageReference] fails.
     * @return [ImageReference] with path to the new image.
     */
    suspend fun update(image: ImageByteArray, oldImageReference: ImageReference): ImageReference

    /**
     * Read image from the storage based on the [ImageReference].
     * @return [ImageReference] with path to the new image, _null_ if image was not found.
     */
    suspend fun read(imageReference: ImageReference): ImageByteArray?
}
