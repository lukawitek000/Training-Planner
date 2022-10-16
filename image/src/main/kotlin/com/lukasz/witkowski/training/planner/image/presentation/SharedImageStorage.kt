package com.lukasz.witkowski.training.planner.image.presentation

import com.lukasz.witkowski.training.planner.image.application.ImageService

/**
 * Image storage that uses two repositories to store image and its reference.
 */
class SharedImageStorage private constructor(private val imageService: ImageService) :
    ImageStorage {
    override fun saveImage(image: Image): ImageReference {
        return imageService.saveImage(image.toImageByteArray())
    }

    override fun readImage(imageId: ImageId): Image {
        return imageService.readImage(imageId)
    }

    override fun readImageReference(imageId: ImageId): ImageReference {
        return imageService.readImageReference(imageId)
    }

    override fun updateImage(image: Image): ImageReference {
        return imageService.updateImage(image.toImageByteArray())
    }

    override fun deleteImage(imageId: ImageId, ownerId: String) {
        imageService.deleteImage(imageId, ownerId)
    }
}
