package com.lukasz.witkowski.training.planner.image.application

import com.lukasz.witkowski.training.planner.image.domain.ImageByteArray
import com.lukasz.witkowski.training.planner.image.presentation.Image
import com.lukasz.witkowski.training.planner.image.presentation.ImageId
import com.lukasz.witkowski.training.planner.image.presentation.ImageReference

internal class ImageService {
    fun saveImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    fun readImage(imageId: ImageId): Image {
        TODO("Not yet implemented")
    }

    fun readImageReference(imageId: ImageId): ImageReference {
        TODO("Not yet implemented")
    }

    fun updateImage(image: ImageByteArray): ImageReference {
        TODO("Not yet implemented")
    }

    fun deleteImage(imageId: ImageId, ownerId: String) {
        TODO("Not yet implemented")
    }

}