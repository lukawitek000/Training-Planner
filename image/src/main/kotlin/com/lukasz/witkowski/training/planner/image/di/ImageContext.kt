package com.lukasz.witkowski.training.planner.image.di

import android.content.Context
import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase

internal class ImageContext(context: Context, directoryName: String) {
    private val database = ImageReferenceDatabase.getInstance(context)
    val imageRepository: ImageRepository = InternalStorageImageRepository(context, directoryName)
    val imageReferenceRepository: ImageReferenceRepository = DbImageReferenceRepository(database.imageReferenceDao())
    val checksumCalculator: ChecksumCalculator = Adler32ChecksumCalculator()
}
