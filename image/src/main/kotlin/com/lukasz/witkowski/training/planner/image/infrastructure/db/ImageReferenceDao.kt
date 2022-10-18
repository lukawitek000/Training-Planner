package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
internal interface ImageReferenceDao {

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE id=:imageId")
    suspend fun getImageReference(imageId: String): DbImageReference

    @Transaction
    suspend fun insert(dbImageReferenceWithOwners: ImageReferenceWithOwners) {
        insert(dbImageReferenceWithOwners.imageReference)
        for (imgOwner in dbImageReferenceWithOwners.owners) {
            insert(imgOwner)
        }
    }

    @Insert
    suspend fun insert(dbImageReference: DbImageReference): Long

    @Insert
    suspend fun insert(dbImageOwner: DbImageOwner): Long
}