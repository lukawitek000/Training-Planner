package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.image.ImageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class CreateExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    private val categoriesCollection: CategoriesCollection,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), CategoriesCollection by categoriesCollection {

    private val _exerciseId = savedStateHandle.get<String>("exerciseId")
    protected open val exerciseId = _exerciseId?.let { ExerciseId(it) }

    val name = savedStateHandle.getStateFlow(NAME_KEY, "")
    val description = savedStateHandle.getStateFlow(DESCRIPTION_KEY, "")
    val category = savedStateHandle.getStateFlow(CATEGORY_KEY, Category())
    val image: StateFlow<ImageBitmap?> = savedStateHandle.getStateFlow(IMAGE_KEY, null) // investigate how to save bitmap it seems to be to big

    protected val _savingState = MutableStateFlow<ResultHandler<Boolean>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Boolean>> = _savingState

    fun onExerciseNameChange(newName: String) {
        savedStateHandle[NAME_KEY] = newName
    }

    fun onExerciseDescriptionChange(newDescription: String) {
        savedStateHandle[DESCRIPTION_KEY] = newDescription
    }

    fun onCategorySelected(newCategoryIndex: Int) {
        savedStateHandle[CATEGORY_KEY] = allCategories[newCategoryIndex]
    }

    fun onImageChange(bitmap: Bitmap) {
        savedStateHandle[IMAGE_KEY] = ImageBitmap(bitmap)
    }

    protected fun createExerciseConfiguration(): ExerciseConfiguration {
        return ExerciseConfiguration(
            name = name.value,
            description = description.value,
            category = CategoryMapper.toExerciseCategory(category.value),
            image = image.value?.let { ImageMapper.toImageByteArray(it) }
        )
    }

    open fun createExercise() {
        val exerciseConfig = createExerciseConfiguration()
        saveExercise(exerciseConfig)
    }

    private fun saveExercise(exerciseConfig: ExerciseConfiguration) {
        asynchronousOperation("Saving exercise failed") {
            exerciseService.saveExercise(exerciseConfig)
        }
    }

    fun asynchronousOperation(errorMessage: String, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _savingState.value = ResultHandler.Loading
                operation()
                _savingState.value = ResultHandler.Success(true)
            } catch (e: Exception) {
                _savingState.value = ResultHandler.Error(message = errorMessage)
            }
        }
    }

    private companion object {
        const val NAME_KEY = "name_key"
        const val DESCRIPTION_KEY = "description_key"
        const val CATEGORY_KEY = "category_key"
        const val IMAGE_KEY = "image_key"
    }
}
