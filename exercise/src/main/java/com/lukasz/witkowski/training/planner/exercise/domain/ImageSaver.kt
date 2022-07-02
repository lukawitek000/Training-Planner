package com.lukasz.witkowski.training.planner.exercise.domain

import java.io.FileNotFoundException

interface ImageSaver {
    /**
     * Save [Image] with the provided file name.
     * @throws [Exception] if saving the [Image] fails.
     */
    fun save(image: Image, fileName: String)

    /**
     * Read [Image] from the storage.
     * @throws [FileNotFoundException] if file has not been found.
     */
    fun read(fileName: String): Image
}
