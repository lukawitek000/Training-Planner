package com.lukasz.witkowski.training.planner.exercise.domain

import java.io.IOException

interface ImageRepository {
    /**
     * Save [Image] with the provided file name.
     * @throws [Exception] if saving the [Image] fails.
     * @return [Image] with path to the image.
     */
    suspend fun save(image: ImageByteArray): ImageReference

    /**
     * Deletes image which was saved under the provided name.
     * @throws [IOException] if the file could not be deleted.
     * @return _true_ if the file was deleted and _false_ otherwise.
     */
    suspend fun delete(fileName: String): Boolean

    /**
     * Update file with new [ImageByteArray].
     * If [ImageByteArray] is null, then it deletes old image file.
     * @throws [Exception] if updating the [Image] fails.
     * @return [Image] with path to the new image.
     */
    suspend fun update(image: ImageByteArray?, oldFileName: String): ImageReference?
}
