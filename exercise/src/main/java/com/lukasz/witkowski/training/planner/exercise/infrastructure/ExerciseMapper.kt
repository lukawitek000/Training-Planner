package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

internal object ExerciseMapper {

    fun toDbExercise(exercise: Exercise): DbExercise {
        return DbExercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            categoryId = exercise.category.ordinal,
            image = exercise.image?.data?.decodeToBitmap()?.compressToByteArray()
        )
    }

    fun toExercise(dbExercise: DbExercise): Exercise {
        return Exercise(
            id = dbExercise.id,
            name = dbExercise.name,
            description = dbExercise.description,
            category = ExerciseCategory.values()[dbExercise.categoryId],
            image = dbExercise.image?.let { Image(it) }
        )
    }

    private fun ExerciseCategory.toText() = name

    private fun String.toCategory() = ExerciseCategory.valueOf(this.uppercase())

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