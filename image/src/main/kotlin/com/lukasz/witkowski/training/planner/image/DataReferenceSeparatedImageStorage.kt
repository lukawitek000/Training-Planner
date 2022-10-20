package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository

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
            ?: throw Exception("Image for id ${imageId.value} not found")
    }

    override fun readImageReference(imageId: ImageId): ImageReference {
        TODO("Not yet implemented")
    }

    override fun updateImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    override suspend fun deleteImage(imageId: ImageId, ownerId: String): Boolean {
        val imageReference = imageReferenceRepository.readByOwnerId(ownerId) ?: throw ImageNotFoundException(imageId)
        val isImageDeleted = imageRepository.delete(imageReference)
        imageReferenceRepository.delete(imageReference)
        return false
    }
}
