package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ImageByteArray
import com.lukasz.witkowski.training.planner.exercise.domain.ImageReference
import com.lukasz.witkowski.training.planner.exercise.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// TODO separate service for images, load async
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageRepository: ImageRepository
) {
    suspend fun saveExercise(exercise: Exercise) {
        exerciseRepository.insert(exercise)
    }

    suspend fun saveImage(image: ImageByteArray): ImageReference {
        return imageRepository.save(image)
    }

    suspend fun updateImage(image: ImageByteArray?, oldImageName: String): ImageReference? {
        return imageRepository.update(image, oldImageName)
    }

    // TODO Should be all exercises taken from domain and then filter here, or the filtration should be made in infra (SQL query)? (less data transmission)
    fun getExercisesFromCategories(categories: List<ExerciseCategory>): Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise ->
                categories.contains(exercise.category) || categories.isEmpty()
            }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        val deleteExercise = async {
            exerciseRepository.delete(exercise)
        }
        val deleteImage = async {
            imageRepository.delete(exercise.getImageName())
        }
        deleteExercise.await()
        deleteImage.await()
    }

    fun getExerciseById(id: ExerciseId): Flow<Exercise> {
        return exerciseRepository.getById(id)
    }

    suspend fun updateExercise(exercise: Exercise): Boolean {
        return exerciseRepository.updateExercise(exercise)
    }

    private fun Exercise.getImageName(): String = "${id.value}_image"
}
