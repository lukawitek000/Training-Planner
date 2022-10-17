package com.lukasz.witkowski.training.planner.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

internal fun Image.toImageByteArray(): ImageByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val compressedByteArray = compressImage(outputStream)
    return ImageByteArray(imageId, ownerId, compressedByteArray)
}

internal fun ImageByteArray.toImage(): Image {
    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
    return Image(imageId, ownerId, bitmap)
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
