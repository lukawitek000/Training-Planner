package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageMapper {
    fun toImageByteArray(image: ImageBitmap): ImageByteArray {
        val outputStream = ByteArrayOutputStream()
        image.bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_100, outputStream)
        val byteArray = outputStream.toByteArray()
        return ImageByteArray(image.imageId, image.ownersIds, byteArray)
    }

    fun toBitmapImage(imageByteArray: ImageByteArray): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray.data, 0, imageByteArray.data.size)
        return ImageBitmap(bitmap)
    }

    private const val QUALITY_100 = 100
}
