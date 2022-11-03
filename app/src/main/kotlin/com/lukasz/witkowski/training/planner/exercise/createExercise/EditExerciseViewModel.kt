package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageMapper
import com.lukasz.witkowski.training.planner.image.ImageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise

@HiltViewModel
class EditExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : CreateExerciseViewModel(exerciseService, categoriesCollection, savedStateHandle) {

    private lateinit var initialExercise: Exercise

    init {
        viewModelScope.launch {
            exerciseService.getExerciseById(exerciseId).collect { domainExercise ->
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
        return ExerciseMapper.toPresentationExercise(domainExercise, imageReference)
    }

    private suspend fun loadBitmap(imageId: ImageId): Bitmap {
        val imageByteArray = exerciseService.readImage(imageId)
        return ImageMapper.toImage(imageByteArray).bitmap
    }

    override fun createExercise() {
        viewModelScope.launch {
            val exercise = Exercise(
                id = exerciseId,
                name = name.value,
                description = description.value,
                category = category.value
            )
            updateExercise(exercise)
        }
    }

    private suspend fun updateExercise(exercise: Exercise) {
        try {
            _savingState.value = ResultHandler.Loading
            val exerciseConfiguration = ExerciseMapper.toExerciseConfiguration(exercise, image.value)
            exerciseService.updateExercise(exerciseId, exerciseConfiguration, null)
            _savingState.value = ResultHandler.Success(true)
        } catch (e: Exception) {
            _savingState.value = ResultHandler.Error(message = "Updating exercise has failed")
        }
    }
}
