package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ImageReference
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.ImageFactory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : CreateExerciseViewModel(exerciseService, categoriesCollection, savedStateHandle) {

    private val _exerciseId = savedStateHandle.get<String>("exerciseId")!!
    private val exerciseId: ExerciseId
        get() = ExerciseId(_exerciseId)

    private lateinit var initialExercise: Exercise

    init {
        viewModelScope.launch {
            exerciseService.getExerciseById(exerciseId).collect { domainExercise ->
                initialExercise = ExerciseMapper.toPresentationExercise(domainExercise)
                onExerciseNameChange(initialExercise.name)
                onExerciseDescriptionChange(initialExercise.description)
                val index = allCategories.indexOf(initialExercise.category)
                if (index >= 0) {
                    onCategorySelected(index)
                }
                initialExercise.image?.let {
                    val imageBitmap = loadBitmap(it)
                    onImageChange(imageBitmap)
                }
            }
        }
    }

    private fun loadBitmap(imageReference: ImageReference): Bitmap {
        return BitmapFactory.decodeFile(imageReference.absolutePath)
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
            val imageReference = updateImage()
            val domainExercise = ExerciseMapper.toDomainExercise(exercise, imageReference)
            val isUpdateSuccessful = exerciseService.updateExercise(domainExercise)
            if(!isUpdateSuccessful) throw Exception("Updating exercise has failed")
            _savingState.value =
                ResultHandler.Success(isUpdateSuccessful)
        } catch (e: Exception) {
            _savingState.value = ResultHandler.Error(message = "Updating exercise has failed")
        }
    }

    private suspend fun updateImage(): ImageReference? {
        val initialExerciseBitmap = initialExercise.image?.let { loadBitmap(it) }
        val currentBitmap = image.value
        return if(areBitmapsDifferent(initialExerciseBitmap, currentBitmap)) {
            val currentByteArray = currentBitmap?.let { ImageFactory.fromBitmap(it) }
            initialExercise.image?.let { exerciseService.updateImage(currentByteArray, it.imageName) }
        } else {
            null
        }
    }

    private fun areBitmapsDifferent(
        initialExerciseBitmap: Bitmap?,
        currentBitmap: Bitmap?
    ) = initialExerciseBitmap?.sameAs(currentBitmap) == false
}