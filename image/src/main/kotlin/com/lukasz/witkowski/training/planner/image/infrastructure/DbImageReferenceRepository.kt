package com.lukasz.witkowski.training.planner.image.infrastructure

import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.presentation.ImageId
import com.lukasz.witkowski.training.planner.image.presentation.ImageReference

internal class DbImageReferenceRepository: ImageReferenceRepository {
    override fun save(imageReference: ImageReference): ImageId? {
        TODO("Not yet implemented")
    }

    override fun delete(imageReference: ImageReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun update(
        newImageReference: ImageReference,
        oldImageReference: ImageReference
    ): ImageId? {
        TODO("Not yet implemented")
    }

    override fun read(ownerId: String): ImageReference {
        TODO("Not yet implemented")
    }
}