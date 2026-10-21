package com.lukasz.witkowski.training.planner.exercise.application

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.toImageConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageStorage: ImageStorage
) {
    suspend fun saveExercise(exerciseConfiguration: ExerciseConfiguration): ExerciseId {
        val exerciseId = ExerciseId.create()
        val imageReferences = if (exerciseConfiguration.images.isNotEmpty()) {
            saveImages(exerciseConfiguration.images, exerciseId)
        } else {
            emptyList()
        }
        val exerciseDetails =
            ExerciseFactory.create(exerciseConfiguration, imageReferences.map { it.imageId }, exerciseId)
        exerciseRepository.insert(exerciseDetails)
        return exerciseId
    }

    suspend fun updateExercise(
        exerciseConfiguration: ExerciseConfiguration,
        exerciseId: ExerciseId
    ): ExerciseId {
        val existing = exerciseRepository.getExerciseDetailsById(exerciseId).firstOrNull()
        if (existing?.exercise?.metadata?.source is ExerciseSource.Bundled) {
            throw IllegalStateException("Bundled exercises cannot be modified")
        }
        val imageIds = if (exerciseConfiguration.images.isNotEmpty()) {
            saveImages(exerciseConfiguration.images, exerciseId).map { it.imageId }
        } else {
            existing?.exercise?.imageIds ?: emptyList()
        }
        val metadata = existing?.exercise?.metadata?.copy(
            syncState = ExerciseSyncState.PENDING_UPLOAD
        ) ?: ExerciseMetadata(
            source = ExerciseSource.UserCreated(ExerciseOwner.Local()),
            syncState = ExerciseSyncState.PENDING_UPLOAD
        )
        val exerciseDetails = ExerciseFactory.create(
            exerciseConfiguration = exerciseConfiguration,
            imageIds = imageIds,
            exerciseId = exerciseId,
            metadata = metadata
        )
        exerciseRepository.updateExercise(exerciseDetails)
        return exerciseId
    }

    private suspend fun saveImages(
        images: List<ImageByteArray>,
        exerciseId: ExerciseId
    ): List<ImageReference> {
        val imageConfigurations = images.map { it.toImageConfiguration(exerciseId.value) }
        return imageStorage.saveImages(imageConfigurations)
    }

    private suspend fun saveImage(
        imageByteArray: ImageByteArray,
        exerciseId: ExerciseId
    ): ImageReference {
        val imageConfiguration = imageByteArray.toImageConfiguration(exerciseId.value)
        return imageStorage.saveImage(imageConfiguration)
    }

    private suspend fun deleteImage(imageId: ImageId, exerciseId: ExerciseId) {
        imageStorage.deleteImage(imageId, exerciseId.value)
    }

    fun queryExercises(exerciseQuery: ExerciseQuery): Flow<PagingData<Exercise2>> =
        exerciseRepository.queryExercises(exerciseQuery)

    suspend fun deleteExercise(exerciseId: ExerciseId) {
        val existing = exerciseRepository.getExerciseDetailsById(exerciseId).firstOrNull()
        if (existing?.exercise?.metadata?.source is ExerciseSource.Bundled) {
            throw IllegalStateException("Bundled exercises cannot be deleted")
        }
        exerciseRepository.delete(exerciseId)
    }

    fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> =
        exerciseRepository.getExerciseDetailsById(id)

    private suspend fun updateImage(
        imageByteArray: ImageByteArray?,
        oldImageId: ImageId?,
        exerciseId: ExerciseId
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

    suspend fun readImageReferences(exerciseId: ExerciseId): List<ImageReference> {
        val fromStorage = imageStorage.readImageReferences(exerciseId.value)
        if (fromStorage.isNotEmpty()) {
            return fromStorage
        }
        val details = exerciseRepository.getExerciseDetailsById(exerciseId).firstOrNull()
        return details?.exercise?.imageIds?.mapNotNull { imageId ->
            imageStorage.readImageReference(imageId)
        } ?: emptyList()
    }
}
