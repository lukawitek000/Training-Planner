package com.lukasz.witkowski.training.planner.image.presentation

import android.graphics.Bitmap

data class Image(
    val imageId: ImageId,
    val ownerId: String,
    val bitmap: Bitmap
)
