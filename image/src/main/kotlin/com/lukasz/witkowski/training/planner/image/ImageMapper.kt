package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageMapper {

    fun toImageByteArray(image: ImageBitmap): ImageByteArray {
        val outputStream = ByteArrayOutputStream()
        image.bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_100, outputStream)
        val byteArray = outputStream.toByteArray()
        return ImageByteArray(byteArray)
    }

    fun toBitmapImage(image: Image): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(image.data, 0, image.data.size)
        return ImageBitmap(bitmap)
    }

    fun toImageConfiguration(imageByteArray: ImageByteArray, ownerId: String): ImageConfiguration {
        return ImageConfiguration(imageByteArray.data, ownerId)
    }

    private const val QUALITY_100 = 100
}
