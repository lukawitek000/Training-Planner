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
        return handleImageReferenceSavingResult(imageId, imageReference)
    }

    private fun handleImageReferenceSavingResult(
        imageId: ImageId?,
        imageReference: ImageReference
    ): ImageReference {
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

    override suspend fun deleteImage(imageId: ImageId, ownerId: String): Boolean {
        val imageReference =
            imageReferenceRepository.readByOwnerId(ownerId) ?: throw ImageNotFoundException(imageId)
        val deletedReference = imageReferenceRepository.delete(imageReference)
        var isImageDeleted = true
        if (!doesAnyReferenceExist(imageId)) {
            isImageDeleted = imageRepository.delete(imageReference)
            if (isImageDeleted.not()) {
                Timber.w("Failed to delete image")
            }
        }
        return deletedReference && isImageDeleted
    }

    private suspend fun doesAnyReferenceExist(imageId: ImageId): Boolean {
        return imageReferenceRepository.read(imageId) != null
    }

    override suspend fun updateImage(imageId: ImageId, newImage: ImageByteArray): ImageReference {
        val uniqueNewImage = generateUniqueIdIfNeeded(imageId, newImage)
        val oldImageReference =
            imageReferenceRepository.read(imageId) ?: return saveImage(uniqueNewImage)
        if (shouldUpdateImageForAllOwners(oldImageReference, uniqueNewImage)) {
            return updateImageForAllOwners(uniqueNewImage, oldImageReference)
        }
        val newImageReference = imageRepository.save(uniqueNewImage)
        val newImageId = imageReferenceRepository.update(newImageReference, oldImageReference)
        return handleImageReferenceSavingResult(newImageId, newImageReference)
    }

    private fun generateUniqueIdIfNeeded(
        oldImageId: ImageId,
        newImage: ImageByteArray
    ): ImageByteArray {
        return if (oldImageId == newImage.imageId) {
            newImage.copy(imageId = ImageId.create())
        } else {
            newImage
        }
    }

    private fun shouldUpdateImageForAllOwners(
        oldImageReference: ImageReference,
        newImage: ImageByteArray
    ): Boolean {
        val oldOwners = oldImageReference.ownersIds
        val newImageOwners = newImage.ownersIds
        return oldOwners.containsAll(newImageOwners) && newImageOwners.containsAll(oldOwners)
    }

    private suspend fun updateImageForAllOwners(
        newImage: ImageByteArray,
        oldImageReference: ImageReference
    ): ImageReference {
        val newImageReference = imageRepository.update(newImage, oldImageReference)
        imageReferenceRepository.delete(oldImageReference)
        val newImageId = imageReferenceRepository.save(newImageReference)
        return handleImageReferenceSavingResult(newImageId, newImageReference)
    }
}
