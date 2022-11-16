package com.lukasz.witkowski.training.planner.image

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase

object ImageStorageFactory {
    fun create(context: Context, directoryName: String): ImageStorage {
        val database = Room.databaseBuilder(
            context,
            ImageReferenceDatabase::class.java, "image-references-database"
        ).fallbackToDestructiveMigration().build()
        val imageRepository = DbImageReferenceRepository(database.imageReferenceDao())
        val imageReferenceRepository = InternalStorageImageRepository(context, directoryName)
        val checksumCalculator = Adler32ChecksumCalculator()
        return DataReferenceSeparatedImageStorage(imageReferenceRepository, imageRepository, checksumCalculator)
    }
}
