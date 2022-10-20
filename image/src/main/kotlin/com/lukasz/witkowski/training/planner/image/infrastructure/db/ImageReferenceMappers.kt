package com.lukasz.witkowski.training.planner.image.infrastructure.db

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference

internal fun ImageReference.toDbImageReferenceWithOwners(): DbImageReferenceWithOwners {
    val imageId = imageId.value
    val dbImageReference = DbImageReference(imageId, path)
    val owners = ownersIds.map { DbImageOwner(it, imageId) }
    return DbImageReferenceWithOwners(dbImageReference, owners)
}

internal fun DbImageReferenceWithOwners.toImageReference(): ImageReference {
    val id = ImageId(imageReference.id)
    val path = imageReference.path
    val ownersIds = owners.map { it.ownerId }
    return ImageReference(id, ownersIds, path)
}

internal fun DbImageReference.toImageReference(ownerId: String): ImageReference {
    val imageId = ImageId(id)
    return ImageReference(imageId, listOf(ownerId), path)
}
