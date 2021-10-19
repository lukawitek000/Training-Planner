package com.lukasz.witkowski.shared.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.lukasz.witkowski.shared.models.Category
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromCategoryToString(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun fromStringToCategory(name: String): Category {
        val categories = Category::class.sealedSubclasses.map { it.objectInstance as Category }

        return categories.firstOrNull {
            it.name == name
        } ?: Category.Legs
    }

    @TypeConverter
    fun fromBitmapToByteArray(bitmap: Bitmap?): ByteArray? {
        if(bitmap == null) {
            return null
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun fromByteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        if(byteArray == null) {
            return null
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }



}