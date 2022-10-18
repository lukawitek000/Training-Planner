package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toDbImageReference
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toImageReference

internal class DbImageReferenceRepository(
    private val imageReferenceDao: ImageReferenceDao
): ImageReferenceRepository {
    override suspend fun save(imageReference: ImageReference): ImageId? {
        val modifiedRows = imageReferenceDao.insert(imageReference.toDbImageReference())
        return if(modifiedRows == 1L) {
            imageReference.imageId
        } else {
            null
        }
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
        val dbImageReference = imageReferenceDao.getImageReference(imageId.value)
        return dbImageReference.toImageReference()
    }
}