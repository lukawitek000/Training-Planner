package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object ImageMapper {
    fun toImageByteArray(image: Image): ImageByteArray {
        val outputStream = ByteArrayOutputStream()
        image.bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return ImageByteArray(image.imageId, image.ownerId, byteArray)
    }

    fun toImage(imageByteArray: ImageByteArray): Image {
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray.data, 0, imageByteArray.data.size)
        return Image(imageByteArray.imageId, imageByteArray.ownerId, bitmap)
    }
}
