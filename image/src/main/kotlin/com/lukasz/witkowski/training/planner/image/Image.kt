package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap

data class Image(
    val imageId: ImageId,
    val ownersIds: List<String>,
    val bitmap: Bitmap
)
