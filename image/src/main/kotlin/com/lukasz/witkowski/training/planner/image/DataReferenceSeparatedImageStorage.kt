package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Image storage that uses two repositories to store image and its reference.
 */
internal class DataReferenceSeparatedImageStorage constructor(
    private val imageRepository: ImageRepository,
    private val imageReferenceRepository: ImageReferenceRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    ImageStorage {

    override suspend fun saveImage(image: ImageByteArray): ImageReference = withContext(ioDispatcher) {
        val imageReference = imageRepository.save(image)
        val imageId = imageReferenceRepository.save(imageReference)
        if(imageId != null) {
            imageReference
        } else {
            throw Exception("Failed to save the image")
        }
    }

    override suspend fun readImage(imageId: ImageId): ImageByteArray = withContext(ioDispatcher) {
        val imageReference = imageReferenceRepository.read(imageId)
        imageReference?.let { imageRepository.read(it) } ?: throw Exception("Image for id ${imageId.value} not found")
    }

    override fun readImageReference(imageId: ImageId): ImageReference {
        TODO("Not yet implemented")
    }

    override fun updateImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    override fun deleteImage(imageId: ImageId, ownerId: String) {
        TODO("Not yet implemented")
    }
}
