package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
@Suppress("TooManyFunctions")
internal interface ImageReferenceDao {

    @Transaction
    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE id=:imageId")
    suspend fun getImageReferenceWithOwners(imageId: String): DbImageReferenceWithOwners?

    @Transaction
    suspend fun insert(dbImageReferenceWithOwners: DbImageReferenceWithOwners) {
        insert(dbImageReferenceWithOwners.imageReference)
        for (imgOwner in dbImageReferenceWithOwners.owners) {
            insert(imgOwner)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbImageReference: DbImageReference): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("SELECT * FROM DBIMAGEOWNER WHERE imageId=:imageId")
    suspend fun getOwnersOfImage(imageId: String): List<DbImageOwner>?

    @Query("DELETE FROM DBIMAGEOWNER WHERE ownerId in (:ownersIds)")
    suspend fun deleteImageOwners(ownersIds: List<String>): Int

    @Query("DELETE FROM DbImageReference WHERE id=:imageId")
    suspend fun deleteImageReference(imageId: String): Int

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE checksum=:checksum")
    suspend fun getImageReferencesByChecksum(checksum: Long): List<DbImageReference>
}
