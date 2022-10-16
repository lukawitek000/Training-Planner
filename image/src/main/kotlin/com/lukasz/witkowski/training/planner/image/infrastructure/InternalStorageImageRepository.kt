package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import com.lukasz.witkowski.training.planner.image.presentation.ImageByteArray
import com.lukasz.witkowski.training.planner.image.presentation.ImageReference

class InternalStorageImageRepository : ImageRepository {
    override suspend fun save(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    override suspend fun delete(imageReference: ImageReference): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        image: ImageByteArray?,
        oldImageReference: ImageReference
    ): ImageReference? {
        TODO("Not yet implemented")
    }

    override suspend fun read(imageReference: ImageReference): ImageByteArray? {
        TODO("Not yet implemented")
    }
}
