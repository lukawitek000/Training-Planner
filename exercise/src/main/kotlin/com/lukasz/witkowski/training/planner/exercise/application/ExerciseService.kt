package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageStorage: ImageStorage
) {

    suspend fun saveExercise(exerciseConfiguration: ExerciseConfiguration) {
        val imageReference = exerciseConfiguration.image?.let { saveImage(it) }
        val exercise = createExercise(exerciseConfiguration, imageReference?.imageId)
        exerciseRepository.insert(exercise)
    }

    private suspend fun saveImage(image: ImageByteArray): ImageReference {
        return imageStorage.saveImage(image)
    }

    suspend fun updateImage(image: ImageByteArray, oldImageId: ImageId): ImageReference {
        return imageStorage.updateImage(oldImageId, image)
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
        previousImage: ImageByteArray?
    ): Boolean {
        val exercise = createExercise(exerciseConfiguration, null, exerciseId)// TODO add check in image storage based on checksum
        return exerciseRepository.updateExercise(exercise)
    }

    suspend fun readImage(imageId: ImageId): ImageByteArray {
        return imageStorage.readImage(imageId)
    }

    suspend fun readImageReference(imageId: ImageId): ImageReference? {
        return imageStorage.readImageReference(imageId)
    }

    private fun createExercise(
        exerciseConfiguration: ExerciseConfiguration,
        imageId: ImageId?,
        exerciseId: ExerciseId = ExerciseId.create()
    ): Exercise {
        return Exercise(
            exerciseId,
            exerciseConfiguration.name,
            exerciseConfiguration.description,
            exerciseConfiguration.category,
            imageId
        )
    }
}
