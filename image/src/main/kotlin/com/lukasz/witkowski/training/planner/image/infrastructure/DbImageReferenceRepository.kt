package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.domain.ImageReference
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.DbImageOwner
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toDbImageReferenceWithOwners
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toImageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DbImageReferenceRepository(
    private val imageReferenceDao: ImageReferenceDao
) : ImageReferenceRepository {
    override suspend fun save(imageReference: ImageReference): ImageId {
        imageReferenceDao.insert(imageReference.toDbImageReferenceWithOwners())
        return imageReference.imageId
    }

    override suspend fun delete(imageReference: ImageReference): Boolean {
        val imageId = imageReference.imageId
        val ownersIds = imageReference.ownersIds
        var isImageReferenceDeletedSuccessfully = true
        if (areAllImageOwners(imageId, ownersIds)) {
            val deletedRows = imageReferenceDao.deleteImageReference(imageId.value)
            isImageReferenceDeletedSuccessfully = (deletedRows == ONE_ROW)
        }
        val deletedRows = imageReferenceDao.deleteImageOwners(imageReference.ownersIds)
        return (deletedRows == ownersIds.size) && isImageReferenceDeletedSuccessfully
    }

    override suspend fun areAllImageOwners(imageId: ImageId, ownersIds: List<String>): Boolean {
        val allImageOwners =
            imageReferenceDao.getOwnersOfImage(imageId.value)?.map { it.ownerId } ?: emptyList()
        return allImageOwners.containsAll(ownersIds) && ownersIds.containsAll(allImageOwners)
    }

    override suspend fun isImageAlreadySaved(checksum: Long): Boolean = withContext(Dispatchers.IO) {
        val imagesWithEqualChecksum = imageReferenceDao.getImageReferencesByChecksum(checksum)
        imagesWithEqualChecksum.isNotEmpty()
    }

    override suspend fun addOwnerToImage(checksum: Long, ownerId: String): ImageReference = withContext(Dispatchers.IO) {
        val sameImage = imageReferenceDao.getImageReferencesByChecksum(checksum).first()
        imageReferenceDao.insert(DbImageOwner(ownerId, sameImage.id))
        val owners = imageReferenceDao.getOwnersOfImage(sameImage.id)
        ImageReference(ImageId(sameImage.id), owners!!.map { it.ownerId }, sameImage.path, checksum)
    }

    override suspend fun update(
        newImageReference: ImageReference,
        oldImageReference: ImageReference
    ): ImageId? {
        val oldImageReferenceWithOwnersToUpdate =
            oldImageReference.copy(ownersIds = newImageReference.ownersIds)
        val wasDeletedSuccessful = delete(oldImageReferenceWithOwnersToUpdate)
        return if (wasDeletedSuccessful) {
            save(newImageReference)
        } else {
            null
        }
    }

    override suspend fun readByOwnerId(ownerId: String): ImageReference? {
        val dbImageReference = imageReferenceDao.getImageReferenceByOwnerId(ownerId)
        return dbImageReference?.toImageReference(ownerId)
    }

    override suspend fun read(imageId: ImageId): ImageReference? {
        val dbImageReference = imageReferenceDao.getImageReferenceWithOwners(imageId.value)
        return dbImageReference?.toImageReference()
    }

    private companion object {
        const val ONE_ROW = 1
    }
}
