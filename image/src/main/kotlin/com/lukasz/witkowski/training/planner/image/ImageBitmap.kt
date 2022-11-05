package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap

data class ImageBitmap(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val bitmap: Bitmap
)
