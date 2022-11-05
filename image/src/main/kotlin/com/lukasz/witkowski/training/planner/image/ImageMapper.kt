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

    fun toImageByteArray(image: Image): ImageByteArray {
        return ImageByteArray(image.data)
    }

    fun toBitmapImage(imageByteArray: ImageByteArray): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray.data, 0, imageByteArray.data.size)
        return ImageBitmap(bitmap)
    }

    fun toImageConfiguration(imageByteArray: ImageByteArray, ownerId: String): ImageConfiguration {
        return ImageConfiguration(imageByteArray.data, ownerId)
    }

    private const val QUALITY_100 = 100
}
