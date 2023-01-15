package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ImageBitmap(
    val bitmap: Bitmap
): java.io.Serializable
