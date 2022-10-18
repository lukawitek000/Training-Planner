package com.lukasz.witkowski.training.planner.image.infrastructure.db

import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference

internal fun ImageReference.toDbImageReference(): DbImageReference {
    return DbImageReference(imageId.value, path)
}

internal fun DbImageReference.toImageReference(): ImageReference {
    return ImageReference(ImageId(id), "ownID", path)
}
