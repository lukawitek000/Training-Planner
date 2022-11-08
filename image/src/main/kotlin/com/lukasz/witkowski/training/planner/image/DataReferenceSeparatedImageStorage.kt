package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import timber.log.Timber
import com.lukasz.witkowski.training.planner.image.domain.Image as DomainImage
import com.lukasz.witkowski.training.planner.image.domain.ImageReference as DomainImageReference

/**
 * Image storage that uses two repositories to store image and its reference.
 */
internal class DataReferenceSeparatedImageStorage constructor(
    private val imageRepository: ImageRepository,
    private val imageReferenceRepository: ImageReferenceRepository,
    private val checksumCalculator: ChecksumCalculator
) : ImageStorage {

    override suspend fun saveImage(imageConfiguration: ImageConfiguration): ImageReference {
        val newImageChecksum = checksumCalculator.calculate(imageConfiguration.toImageByteArray())
        return if (imageReferenceRepository.isImageAlreadySaved(newImageChecksum)) {
            Timber.d("Image is already stored in the storage, added new owner to it.")
            imageReferenceRepository.addOwnerToImage(newImageChecksum, imageConfiguration.ownerId).toImageReference()
        } else {
            val image = createImage(imageConfiguration)
            val imageReference = imageRepository.save(image)
            val imageId = imageReferenceRepository.save(imageReference)
            handleImageReferenceSavingResult(imageId, imageReference)
        }
    }

    private fun handleImageReferenceSavingResult(
        imageId: ImageId?,
        imageReference: DomainImageReference
    ): ImageReference {
        return if (imageId != null) {
            ImageReference(imageId, imageReference.path)
        } else {
            throw ImageSaveFailedException(imageReference.imageId)
        }
    }

    override suspend fun readImage(imageId: ImageId): Image {
        val imageReference = imageReferenceRepository.read(imageId)
        val domainImage = imageReference?.let { imageRepository.read(it) }
        return domainImage?.toImage() ?: throw ImageNotFoundException(imageId)
    }

    override suspend fun readImageReference(imageId: ImageId): ImageReference? {
        val domainImageReference = imageReferenceRepository.read(imageId)
        return domainImageReference?.toImageReference()
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

    override suspend fun updateImage(
        imageId: ImageId,
        newImageConfiguration: ImageConfiguration
    ): ImageReference {
        val newImage = createImage(newImageConfiguration)
        return if (imageReferenceRepository.isImageAlreadySaved(newImage.checksum)) {
            imageReferenceRepository.addOwnerToImage(
                newImage.checksum,
                newImageConfiguration.ownerId
            ).toImageReference()
        } else {
            val oldImageReference = imageReferenceRepository.read(imageId)
            if (oldImageReference == null) {
                saveImage(newImageConfiguration)
            } else {
                val newImageReference = imageRepository.save(newImage)
                val newImageId =
                    imageReferenceRepository.update(newImageReference, oldImageReference)
                handleImageReferenceSavingResult(newImageId, newImageReference)
            }
        }
    }

    private fun createImage(imageConfiguration: ImageConfiguration) =
        DomainImage(
            generateImageId(),
            listOf(imageConfiguration.ownerId),
            imageConfiguration.data,
            checksumCalculator.calculate(ImageByteArray(imageConfiguration.data))
        )

    private fun generateImageId() = ImageId.create()
}
