package com.lukasz.witkowski.training.planner.exercise.application

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.toImageConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageStorage: ImageStorage,
) {
    suspend fun saveExercise(exerciseConfiguration: ExerciseConfiguration): ExerciseId {
        val exerciseId = ExerciseId.create()
        val imageReference = exerciseConfiguration.image?.let { saveImage(it, exerciseId) }
        val exerciseDetails =
            ExerciseFactory.create(exerciseConfiguration, imageReference?.imageId, exerciseId)
        exerciseRepository.insert(exerciseDetails)
        return exerciseId
    }

    suspend fun updateExercise(
        exerciseConfiguration: ExerciseConfiguration,
        exerciseId: ExerciseId
    ): ExerciseId {
        val imageReference = exerciseConfiguration.image?.let { saveImage(it, exerciseId) }
        val exerciseDetails =
            ExerciseFactory.create(exerciseConfiguration, imageReference?.imageId, exerciseId)
        exerciseRepository.updateExercise(exerciseDetails)
        return exerciseId
    }

    private suspend fun saveImage(
        imageByteArray: ImageByteArray,
        exerciseId: ExerciseId,
    ): ImageReference {
        val imageConfiguration = imageByteArray.toImageConfiguration(exerciseId.value)
        return imageStorage.saveImage(imageConfiguration)
    }

    private suspend fun deleteImage(
        imageId: ImageId,
        exerciseId: ExerciseId,
    ) {
        imageStorage.deleteImage(imageId, exerciseId.value)
    }

    fun queryExercises(exerciseQuery: ExerciseQuery): Flow<PagingData<Exercise2>> =
        exerciseRepository.queryExercises(exerciseQuery)

    suspend fun deleteExercise(exerciseId: ExerciseId) {
        exerciseRepository.delete(exerciseId)
    }

    fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails> =
        exerciseRepository.getExerciseDetailsById(id)

    private suspend fun updateImage(
        imageByteArray: ImageByteArray?,
        oldImageId: ImageId?,
        exerciseId: ExerciseId,
    ): ImageReference? {
        val imageConfiguration = imageByteArray?.toImageConfiguration(exerciseId.value)
        return if (oldImageId == null) {
            imageConfiguration?.let { imageStorage.saveImage(imageConfiguration) }
        } else {
            if (imageConfiguration == null) {
                imageStorage.deleteImage(oldImageId, exerciseId.value)
                null // Do not exist after delete
            } else {
                imageConfiguration.let { imageStorage.updateImage(oldImageId, imageConfiguration) }
            }
        }
    }

    suspend fun readImage(imageId: ImageId): Image = imageStorage.readImage(imageId)

    suspend fun readImageReference(imageId: ImageId): ImageReference? =
        imageStorage.readImageReference(imageId)
}
