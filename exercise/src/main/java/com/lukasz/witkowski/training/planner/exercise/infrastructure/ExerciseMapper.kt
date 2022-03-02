package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Category
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.allCategories
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

// TODO The conversion can be made using TypeConverter from Room??
// The mapper is better if the external library would change, mapper has to be adjusted only
object ExerciseMapper {

    fun toDbExercise(exercise: Exercise): DbExercise {
        return DbExercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = exercise.category.toText(),
            image = exercise.image?.compressToByteArray()
        )
    }

    fun toExercise(dbExercise: DbExercise): Exercise {
        return Exercise(
            id = dbExercise.id,
            name = dbExercise.name,
            description = dbExercise.description,
            category = dbExercise.category.toCategory(),
            image = dbExercise.image?.decodeToBitmap()
        )
    }

    private fun Category.toText() = name

    private fun String.toCategory() = allCategories.firstOrNull {
        it.name == this
    } ?: Category.None

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