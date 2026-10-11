package com.lukasz.witkowski.training.planner.image.domain

internal interface ImageRepository {
    /**
     * Save [Image].
     * @throws [Exception] if saving the [Image] fails.
     * @return [ImageReference] with path to the image.
     */
    suspend fun save(image: Image): ImageReference

    /**
     * Deletes image which was saved under the provided name.
     * @return _true_ if the file was deleted and _false_ otherwise.
     */
    suspend fun delete(imageReference: ImageReference): Boolean

    /**
     * Update file with new [Image].
     * @throws [Exception] if updating the [ImageReference] fails.
     * @return [ImageReference] with path to the new image.
     */
    suspend fun update(image: Image, oldImageReference: ImageReference): ImageReference

    /**
     * Read image from the storage based on the [ImageReference].
     * @return [ImageReference] with path to the new image, _null_ if image was not found.
     */
    suspend fun read(imageReference: ImageReference): Image?
}
