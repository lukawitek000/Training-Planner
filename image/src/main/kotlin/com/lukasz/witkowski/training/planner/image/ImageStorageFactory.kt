package com.lukasz.witkowski.training.planner.image

import android.content.Context
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository

object ImageStorageFactory {
    fun create(context: Context, directoryName: String): ImageStorage {
        val imageRepository = DbImageReferenceRepository()
        val imageReferenceRepository = InternalStorageImageRepository(context, directoryName)
        return DataReferenceSeparatedImageStorage(imageReferenceRepository, imageRepository)
    }
}
