package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import timber.log.Timber

/**
 * Image storage that uses two repositories to store image and its reference.
 */
internal class DataReferenceSeparatedImageStorage constructor(
    private val imageRepository: ImageRepository,
    private val imageReferenceRepository: ImageReferenceRepository
) : ImageStorage {

    override suspend fun saveImage(image: ImageByteArray): ImageReference {
        val imageReference = imageRepository.save(image)
        val imageId = imageReferenceRepository.save(imageReference)
        return if (imageId != null) {
            imageReference
        } else {
            throw Exception("Failed to save the image")
        }
    }

    override suspend fun readImage(imageId: ImageId): ImageByteArray {
        val imageReference = imageReferenceRepository.read(imageId)
        return imageReference?.let { imageRepository.read(it) }
            ?: throw ImageNotFoundException(imageId)
    }

    override suspend fun readImageReference(imageId: ImageId): ImageReference? {
        return imageReferenceRepository.read(imageId)
    }

    override fun updateImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    override suspend fun deleteImage(imageId: ImageId, ownerId: String): Boolean {
        val imageReference = imageReferenceRepository.readByOwnerId(ownerId) ?: throw ImageNotFoundException(imageId)
        val deletedReference = imageReferenceRepository.delete(imageReference)
        val doesAnyReferenceExist = imageReferenceRepository.read(imageId) != null
        var isImageDeleted = true
        if(!doesAnyReferenceExist) {
            isImageDeleted = imageRepository.delete(imageReference)
            if(isImageDeleted.not()) {
                Timber.w("Failed to delete image")
            }
        }
        return deletedReference && isImageDeleted
    }
}
