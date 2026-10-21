package com.lukasz.witkowski.training.planner.image.infrastructure.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction

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
    suspend fun getImageReferencesByOwnerId(ownerId: String): List<DbImageReference> {
        val owners = getImageOwnersByOwnerId(ownerId)
        return owners.map { getImageReferenceById(it.imageId) }
    }

    @Transaction
    suspend fun getImageReferenceByOwnerId(ownerId: String): DbImageReference? {
        val owners = getImageOwnersByOwnerId(ownerId)
        val imageId = owners.firstOrNull()?.imageId
        return imageId?.let { getImageReferenceById(it) }
    }

    @Query("SELECT * FROM DBIMAGEOWNER WHERE ownerId=:ownerId")
    suspend fun getImageOwnersByOwnerId(ownerId: String): List<DbImageOwner>

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE id=:imageId")
    suspend fun getImageReferenceById(imageId: String): DbImageReference

    @Query("SELECT * FROM DBIMAGEOWNER WHERE imageId=:imageId")
    suspend fun getOwnersOfImage(imageId: String): List<DbImageOwner>

    @Query("DELETE FROM DBIMAGEOWNER WHERE imageId=:imageId AND ownerId in (:ownersIds)")
    suspend fun deleteImageOwners(imageId: String, ownersIds: List<String>): Int

    @Query("DELETE FROM DBIMAGEOWNER WHERE ownerId in (:ownersIds)")
    suspend fun deleteAllImageOwners(ownersIds: List<String>): Int

    @Query("DELETE FROM DbImageReference WHERE id=:imageId")
    suspend fun deleteImageReference(imageId: String): Int

    @Query("SELECT * FROM DBIMAGEREFERENCE WHERE checksum=:checksum")
    suspend fun getImageReferencesByChecksum(checksum: Long): List<DbImageReference>
}
