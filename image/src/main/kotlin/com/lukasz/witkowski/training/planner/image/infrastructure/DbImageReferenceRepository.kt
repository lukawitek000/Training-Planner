package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference

internal class DbImageReferenceRepository: ImageReferenceRepository {
    override suspend fun save(imageReference: ImageReference): ImageId? {
        TODO("Not yet implemented")
    }

    override fun delete(imageReference: ImageReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun update(
        newImageReference: ImageReference,
        oldImageReference: ImageReference
    ): ImageId? {
        TODO("Not yet implemented")
    }

    override suspend fun readByOwnerId(ownerId: String): ImageReference {
        TODO("Not yet implemented")
    }

    override suspend fun read(imageId: ImageId): ImageReference {
        TODO("Not yet implemented")
    }
}