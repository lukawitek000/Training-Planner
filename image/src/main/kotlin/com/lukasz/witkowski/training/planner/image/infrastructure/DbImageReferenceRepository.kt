package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toDbImageReferenceWithOwners
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toImageReference

internal class DbImageReferenceRepository(
    private val imageReferenceDao: ImageReferenceDao
) : ImageReferenceRepository {
    override suspend fun save(imageReference: ImageReference): ImageId {
        imageReferenceDao.insert(imageReference.toDbImageReferenceWithOwners())
        return imageReference.imageId
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
        val dbImageReference = imageReferenceDao.getImageReferenceWithOwners(imageId.value)
        return dbImageReference.toImageReference()
    }
}