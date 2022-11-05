package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import timber.log.Timber

/**
 * Image storage that uses two repositories to store image and its reference.
 */
internal class DataReferenceSeparatedImageStorage constructor(
    private val imageRepository: ImageRepository,
    private val imageReferenceRepository: ImageReferenceRepository,
    private val checksumCalculator: ChecksumCalculator
) : ImageStorage {

    override suspend fun saveImage(imageConfiguration: ImageConfiguration): ImageReference {
        val image = createImage(imageConfiguration)
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
            throw ImageSaveFailedException(imageReference.imageId)
        }
    }

    override suspend fun readImage(imageId: ImageId): Image {
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

    override suspend fun updateImage(imageId: ImageId, newImageConfiguration: ImageConfiguration): ImageReference {
        val newImage = createImage(newImageConfiguration)
        val oldImageReference =
            imageReferenceRepository.read(imageId)
        return if (oldImageReference == null) {
            saveImage(newImageConfiguration)
        } else if (imageReferenceRepository.areAllImageOwners(imageId, listOf(newImageConfiguration.ownerId))) {
            updateImageForAllOwners(newImage, oldImageReference)
        } else {
            val newImageReference = imageRepository.save(newImage)
            val newImageId = imageReferenceRepository.update(newImageReference, oldImageReference)
            handleImageReferenceSavingResult(newImageId, newImageReference)
        }
    }

    private suspend fun updateImageForAllOwners(
        newImage: Image,
        oldImageReference: ImageReference
    ): ImageReference {
        val newImageReference = imageRepository.update(newImage, oldImageReference)
        imageReferenceRepository.delete(oldImageReference)
        val newImageId = imageReferenceRepository.save(newImageReference)
        return handleImageReferenceSavingResult(newImageId, newImageReference)
    }

    private fun createImage(imageConfiguration: ImageConfiguration) =
        Image(
            generateImageId(),
            listOf(imageConfiguration.ownerId),
            imageConfiguration.data,
            checksumCalculator.calculate(ImageByteArray(imageConfiguration.data))
        )

    private fun generateImageId() = ImageId.create()
}
