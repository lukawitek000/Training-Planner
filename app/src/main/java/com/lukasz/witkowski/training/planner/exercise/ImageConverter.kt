package com.lukasz.witkowski.training.planner.exercise

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object ImageConverter {

    fun toBitmap(image: Image?) = image?.data?.decodeToBitmap()

    fun toByteArray(bitmap: Bitmap?) = bitmap?.compressToByteArray()

    private fun Bitmap.compressToByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, outputStream)
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

    private fun ByteArray.decodeToBitmap() = BitmapFactory.decodeByteArray(this, 0, size)
}