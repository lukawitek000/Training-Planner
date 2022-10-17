package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository

object ImageStorageFactory {
    fun create(): ImageStorage {
        val imageRepository = DbImageReferenceRepository()
        val imageReferenceRepository = InternalStorageImageRepository()
        return DataReferenceSeparatedImageStorage(imageReferenceRepository, imageRepository)
    }
}
