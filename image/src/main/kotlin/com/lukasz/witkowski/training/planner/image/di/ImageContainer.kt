package com.lukasz.witkowski.training.planner.image.di

import android.content.Context
import com.lukasz.witkowski.training.planner.image.DataReferenceSeparatedImageStorage
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase

class ImageContainer(context: Context, directoryName: String) {

    private val imageRepository: ImageRepository by lazy {
        InternalStorageImageRepository(context, directoryName)
    }

    private val imageReferenceRepository: ImageReferenceRepository by lazy {
        val database = ImageReferenceDatabase.getInstance(context)
        DbImageReferenceRepository(database.imageReferenceDao())
    }

    private val checksumCalculator: ChecksumCalculator by lazy {
        Adler32ChecksumCalculator()
    }

    val imageStorage: ImageStorage by lazy {
        DataReferenceSeparatedImageStorage(imageRepository, imageReferenceRepository, checksumCalculator)
    }
}
