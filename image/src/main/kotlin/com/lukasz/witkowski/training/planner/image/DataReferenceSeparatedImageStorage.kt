package com.lukasz.witkowski.training.planner.image

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository

/**
 * Image storage that uses two repositories to store image and its reference.
 */
class DataReferenceSeparatedImageStorage private constructor(private val imageRepository: ImageRepository, private val imageReferenceRepository: ImageReferenceRepository) :
    ImageStorage {
    override fun saveImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    override fun readImage(imageId: ImageId): ImageByteArray {
        TODO("Not yet implemented")
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
