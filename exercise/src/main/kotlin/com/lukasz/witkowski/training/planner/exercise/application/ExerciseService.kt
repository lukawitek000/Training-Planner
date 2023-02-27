package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.toImageConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageStorage: ImageStorage
) {

    suspend fun saveExercise(exerciseConfiguration: ExerciseConfiguration) {
        val exerciseId = generateExerciseId()
        val imageReference = exerciseConfiguration.image?.let { saveImage(it, exerciseId) }
        val exercise = createExercise(exerciseConfiguration, imageReference?.imageId, exerciseId)
        exerciseRepository.insert(exercise)
    }

    private suspend fun saveImage(imageByteArray: ImageByteArray, exerciseId: ExerciseId): ImageReference {
        val imageConfiguration = imageByteArray.toImageConfiguration(exerciseId.value)
        return imageStorage.saveImage(imageConfiguration)
    }

    private suspend fun deleteImage(imageId: ImageId, exerciseId: ExerciseId) {
        imageStorage.deleteImage(imageId, exerciseId.value)
    }

    fun getExercisesFromCategories(categories: List<ExerciseCategory>): Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise ->
                categories.contains(exercise.category) || categories.isEmpty()
            }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
        exercise.imageId?.let { deleteImage(it, exercise.id) }
    }

    suspend fun getExerciseById(id: ExerciseId): Exercise {
        return exerciseRepository.getById(id)
    }

    suspend fun updateExercise(
        exerciseId: ExerciseId,
        exerciseConfiguration: ExerciseConfiguration,
        previousImage: Image?
    ): Boolean {
        val updatedImage = updateImage(exerciseConfiguration.image, previousImage?.imageId, exerciseId)
        val exercise = createExercise(exerciseConfiguration, updatedImage?.imageId, exerciseId)
        return exerciseRepository.updateExercise(exercise)
    }

    private suspend fun updateImage(imageByteArray: ImageByteArray?, oldImageId: ImageId?, exerciseId: ExerciseId): ImageReference? {
        val imageConfiguration = imageByteArray?.toImageConfiguration(exerciseId.value)
        return if(oldImageId == null) {
            imageConfiguration?.let { imageStorage.saveImage(imageConfiguration) }
        } else {
            if(imageConfiguration == null) {
                imageStorage.deleteImage(oldImageId, exerciseId.value)
                null // Do not exist after delete
            } else {
                imageConfiguration.let { imageStorage.updateImage(oldImageId, imageConfiguration) }
            }
        }
    }

    suspend fun readImage(imageId: ImageId): Image {
        return imageStorage.readImage(imageId)
    }

    suspend fun readImageReference(imageId: ImageId): ImageReference? {
        return imageStorage.readImageReference(imageId)
    }

    private fun createExercise(
        exerciseConfiguration: ExerciseConfiguration,
        imageId: ImageId?,
        exerciseId: ExerciseId = generateExerciseId()
    ): Exercise {
        return Exercise(
            exerciseId,
            exerciseConfiguration.name,
            exerciseConfiguration.description,
            exerciseConfiguration.category,
            imageId
        )
    }

    private fun generateExerciseId() = ExerciseId.create()
}
