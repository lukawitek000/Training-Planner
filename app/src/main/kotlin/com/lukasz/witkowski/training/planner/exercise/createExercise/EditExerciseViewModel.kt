package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toPresentationExercise
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.toBitmapImage
import kotlinx.coroutines.launch
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

class EditExerciseViewModel(
    private val exerciseService: ExerciseService,
    categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : CreateExerciseViewModel(exerciseService, categoriesCollection, savedStateHandle) {

    private lateinit var initialExercise: Exercise

    override val exerciseId: ExerciseId
        get() = super.exerciseId
            ?: throw IllegalArgumentException("ExerciseId was to provided to edit screen")

    init {
        viewModelScope.launch {
            val domainExercise = exerciseService.getExerciseById(exerciseId)
            initialExercise = mapToPresentationExercise(domainExercise)
            onExerciseNameChange(initialExercise.name)
            onExerciseDescriptionChange(initialExercise.description)
            setExerciseCategory()
            initialExercise.image?.let {
                val imageBitmap = loadBitmap(it.imageId)
                onImageChange(imageBitmap)
            }
        }
    }

    private fun setExerciseCategory() {
        val index = allCategories.indexOf(initialExercise.category)
        if (index >= 0) {
            onCategorySelected(index)
        }
    }

    private suspend fun mapToPresentationExercise(domainExercise: DomainExercise): Exercise {
        val imageReference = domainExercise.imageId?.let {
            exerciseService.readImageReference(it)
        }
        return domainExercise.toPresentationExercise(imageReference)
    }

    private suspend fun loadBitmap(imageId: ImageId): Bitmap {
        val image = exerciseService.readImage(imageId)
        return image.toBitmapImage().bitmap
    }

    override fun createExercise() {
        val exerciseConfig = createExerciseConfiguration()
        updateExercise(exerciseConfig)
    }

    private fun updateExercise(exerciseConfiguration: ExerciseConfiguration) {
        asynchronousOperation("Updating exercise has failed") {
            exerciseService.updateExercise(exerciseId, exerciseConfiguration, null)
        }
    }
}
