package com.lukasz.witkowski.training.planner.training.infrastructure

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class Converters {

    @TypeConverter
    fun fromBitmapToByteArray(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) {
            return null
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
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

    @TypeConverter
    fun fromByteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        if (byteArray == null) {
            return null
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
