package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room3.Embedded
import androidx.room3.Relation

data class DbImageReferenceWithOwners(
    @Embedded
    val imageReference: DbImageReference,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["imageId"],
    )
    val owners: List<DbImageOwner>,
)
