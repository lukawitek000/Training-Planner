package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
internal interface ImageReferenceDao {

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE id=:imageId")
    suspend fun getImageReferenceWithOwners(imageId: String): DbImageReferenceWithOwners

    @Transaction
    suspend fun insert(dbImageReferenceWithOwners: DbImageReferenceWithOwners) {
        insert(dbImageReferenceWithOwners.imageReference)
        for (imgOwner in dbImageReferenceWithOwners.owners) {
            insert(imgOwner)
        }
    }

    @Insert
    suspend fun insert(dbImageReference: DbImageReference): Long

    @Insert
    suspend fun insert(dbImageOwner: DbImageOwner): Long

    @Transaction
    suspend fun getImageReferenceByOwnerId(ownerId: String): DbImageReference? {
        val imageId = getImageOwnerById(ownerId)?.imageId
        return imageId?.let { getImageReferenceById(it) }
    }

    @Query("SELECT * FROM DBIMAGEOWNER WHERE ownerId=:ownerId")
    suspend fun getImageOwnerById(ownerId: String): DbImageOwner?

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE id=:imageId")
    suspend fun getImageReferenceById(imageId: String): DbImageReference
}