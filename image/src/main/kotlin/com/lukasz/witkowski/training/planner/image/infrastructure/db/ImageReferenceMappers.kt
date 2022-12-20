package com.lukasz.witkowski.training.planner.image.infrastructure.db

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.domain.ImageReference
import java.util.UUID

internal fun ImageReference.toDbImageReferenceWithOwners(): DbImageReferenceWithOwners {
    val imageId = imageId.toString()
    val dbImageReference = DbImageReference(imageId, path, checksum)
    val owners = ownersIds.map { DbImageOwner(it.toString(), imageId) }
    return DbImageReferenceWithOwners(dbImageReference, owners)
}

internal fun DbImageReferenceWithOwners.toImageReference(): ImageReference {
    val id = ImageId(imageReference.id)
    val path = imageReference.path
    val ownersIds = owners.map { UUID.fromString(it.ownerId) }
    return ImageReference(id, ownersIds, path, imageReference.checksum)
}

internal fun DbImageReference.toImageReference(ownerId: String): ImageReference {
    val imageId = ImageId(id)
    return ImageReference(imageId, listOf(UUID.fromString(ownerId)), path, checksum)
}
