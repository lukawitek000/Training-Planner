package com.lukasz.witkowski.training.planner.training.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ImageFactory

object TrainingExerciseMapper {

    fun toDomainTrainingExercise(trainingExercise: TrainingExercise): com.lukasz.witkowski.training.planner.training.domain.TrainingExercise {
        return com.lukasz.witkowski.training.planner.training.domain.TrainingExercise(
            id = trainingExercise.id,
            name = trainingExercise.name,
            description = trainingExercise.description,
            category = CategoryMapper.toExerciseCategory(trainingExercise.category),
            image = trainingExercise.image?.toImage(),
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time,
            restTime = trainingExercise.restTime
        )
    }

    fun toPresentationTrainingExercise(trainingExercise: com.lukasz.witkowski.training.planner.training.domain.TrainingExercise): TrainingExercise {
        return TrainingExercise(
            id = trainingExercise.id,
            name = trainingExercise.name,
            description = trainingExercise.description,
            category = CategoryMapper.toCategory(trainingExercise.category),
            image = trainingExercise.image?.toBitmap(),
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time,
            restTime = trainingExercise.restTime
        )
    }

    private fun Image.toBitmap() = BitmapFactory.decodeByteArray(data, 0, data.size)

    private fun Bitmap.toImage() = ImageFactory.fromBitmap(bitmap = this)
}