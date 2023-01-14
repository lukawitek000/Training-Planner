package com.lukasz.witkowski.training.planner.image

import android.content.Context
import com.lukasz.witkowski.training.planner.image.di.ImageContext

object ImageStorageFactory {
    fun create(context: Context, directoryName: String): ImageStorage {
        val imageContainer = ImageContext(context, directoryName)
        return DataReferenceSeparatedImageStorage(
            imageContainer.imageRepository,
            imageContainer.imageReferenceRepository,
            imageContainer.checksumCalculator
        )
    }
}
