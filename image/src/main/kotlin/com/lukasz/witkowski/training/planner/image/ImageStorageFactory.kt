package com.lukasz.witkowski.training.planner.image

import android.content.Context
import com.lukasz.witkowski.training.planner.image.di.ImageContainer

object ImageStorageFactory {
    fun create(context: Context, directoryName: String): ImageStorage {
        val imageContainer = ImageContainer(context, directoryName)
        return DataReferenceSeparatedImageStorage(
            imageContainer.imageRepository,
            imageContainer.imageReferenceRepository,
            imageContainer.checksumCalculator
        )
    }
}
