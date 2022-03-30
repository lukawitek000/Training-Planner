package com.lukasz.witkowski.training.planner.exercise.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object ImageFactory {
    fun fromBitmap(bitmap: Bitmap): Image {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val compressedByteArray = compressImage(outputStream)
        return Image(compressedByteArray)
    }

    private fun compressImage(outputStream: ByteArrayOutputStream): ByteArray {
        var imageByteArray = outputStream.toByteArray()
        while (imageByteArray.size > 500000) {
            val img = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            val resized = Bitmap.createScaledBitmap(
                img, (img.width * 0.8).roundToInt(),
                (img.height * 0.8).roundToInt(), true
            )
            val stream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.PNG, 70, stream)
            imageByteArray = stream.toByteArray()
        }
        return imageByteArray
    }
}