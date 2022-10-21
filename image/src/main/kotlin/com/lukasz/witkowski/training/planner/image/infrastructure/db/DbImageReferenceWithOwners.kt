package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Embedded
import androidx.room.Relation

internal data class DbImageReferenceWithOwners(
    @Embedded
    val imageReference: DbImageReference,
    @Relation(
        parentColumn = "id",
        entityColumn = "imageId"
    )
    val owners: List<DbImageOwner>
)
