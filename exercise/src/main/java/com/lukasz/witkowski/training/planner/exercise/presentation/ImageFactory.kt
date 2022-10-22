package com.lukasz.witkowski.training.planner.exercise.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.ImageByteArray
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object ImageFactory {
    fun fromBitmap(bitmap: Bitmap, imageId: ImageId = ImageId.create()): ImageByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val compressedByteArray = compressImage(outputStream)
        return ImageByteArray(imageId, compressedByteArray)
    }

    private fun compressImage(outputStream: ByteArrayOutputStream): ByteArray {
        var imageByteArray = outputStream.toByteArray()
        while (imageByteArray.size > COMPRESSED_IMAGE_SIZE) {
            val resized = resizeBitmap(imageByteArray)
            val stream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, stream)
            imageByteArray = stream.toByteArray()
        }
        return imageByteArray
    }

    private fun resizeBitmap(imageByteArray: ByteArray): Bitmap {
        val img = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        return Bitmap.createScaledBitmap(
            img, calculateDesiredReducedSize(img.width),
            calculateDesiredReducedSize(img.height), true
        )
    }

    private fun calculateDesiredReducedSize(size: Int) =
        (size * COMPRESS_SCALE).roundToInt()

    private const val COMPRESSED_IMAGE_SIZE = 500000
    private const val COMPRESS_SCALE = 0.8
    private const val COMPRESS_QUALITY = 70
}
