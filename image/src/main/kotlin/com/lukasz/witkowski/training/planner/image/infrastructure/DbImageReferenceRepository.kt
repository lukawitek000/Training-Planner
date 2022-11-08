package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.domain.ImageReference
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.DbImageOwner
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDao
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toDbImageReferenceWithOwners
import com.lukasz.witkowski.training.planner.image.infrastructure.db.toImageReference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DbImageReferenceRepository(
    private val imageReferenceDao: ImageReferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageReferenceRepository {
    override suspend fun save(imageReference: ImageReference): ImageId = withContext(ioDispatcher) {
        imageReferenceDao.insert(imageReference.toDbImageReferenceWithOwners())
        imageReference.imageId
    }

    override suspend fun delete(imageReference: ImageReference): Boolean = withContext(ioDispatcher) {
        val imageId = imageReference.imageId
        val ownersIds = imageReference.ownersIds
        var isImageReferenceDeletedSuccessfully = true
        if (areAllImageOwners(imageId, ownersIds)) {
            val deletedRows = imageReferenceDao.deleteImageReference(imageId.value)
            isImageReferenceDeletedSuccessfully = (deletedRows == ONE_ROW)
        }
        val deletedRows = imageReferenceDao.deleteImageOwners(imageReference.ownersIds)
        (deletedRows == ownersIds.size) && isImageReferenceDeletedSuccessfully
    }

    override suspend fun areAllImageOwners(imageId: ImageId, ownersIds: List<String>): Boolean = withContext(ioDispatcher) {
        val allImageOwners =
            imageReferenceDao.getOwnersOfImage(imageId.value)?.map { it.ownerId } ?: emptyList()
        allImageOwners.containsAll(ownersIds) && ownersIds.containsAll(allImageOwners)
    }

    override suspend fun isImageAlreadySaved(checksum: Long): Boolean = withContext(ioDispatcher) {
        val imagesWithEqualChecksum = imageReferenceDao.getImageReferencesByChecksum(checksum)
        imagesWithEqualChecksum.isNotEmpty()
    }

    override suspend fun addOwnerToImage(checksum: Long, ownerId: String): ImageReference = withContext(ioDispatcher) {
        val sameImage = imageReferenceDao.getImageReferencesByChecksum(checksum).first()
        imageReferenceDao.insert(DbImageOwner(ownerId, sameImage.id))
        val owners = imageReferenceDao.getOwnersOfImage(sameImage.id)
        ImageReference(ImageId(sameImage.id), owners!!.map { it.ownerId }, sameImage.path, checksum)
    }

    override suspend fun update(
        newImageReference: ImageReference,
        oldImageReference: ImageReference
    ): ImageId? = withContext(ioDispatcher) {
        val oldImageReferenceWithOwnersToUpdate =
            oldImageReference.copy(ownersIds = newImageReference.ownersIds)
        val wasDeletedSuccessful = delete(oldImageReferenceWithOwnersToUpdate)
        if (wasDeletedSuccessful) {
            save(newImageReference)
        } else {
            null
        }
    }

    override suspend fun readByOwnerId(ownerId: String): ImageReference? = withContext(ioDispatcher) {
        val dbImageReference = imageReferenceDao.getImageReferenceByOwnerId(ownerId)
        dbImageReference?.toImageReference(ownerId)
    }

    override suspend fun read(imageId: ImageId): ImageReference? = withContext(ioDispatcher) {
        val dbImageReference = imageReferenceDao.getImageReferenceWithOwners(imageId.value)
        dbImageReference?.toImageReference()
    }

    private companion object {
        const val ONE_ROW = 1
    }
}
