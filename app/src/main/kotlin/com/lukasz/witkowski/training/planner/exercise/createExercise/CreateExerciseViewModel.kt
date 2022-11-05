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
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** StateFlow with SavedStateHandle, do I need it?
 * https://medium.com/mobile-app-development-publication/saving-stateflow-state-in-viewmodel-2ee9ed9b1a83
 */

@HiltViewModel
open class CreateExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    private val categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CategoriesCollection by categoriesCollection {

    private val _exerciseId = savedStateHandle.get<String>("exerciseId")
    protected open val exerciseId = _exerciseId?.let { ExerciseId(it) }

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _category = MutableStateFlow(Category())
    val category: StateFlow<Category> = _category

    private val _image = MutableStateFlow<ImageBitmap?>(null)
    val image: StateFlow<ImageBitmap?> = _image

    protected val _savingState = MutableStateFlow<ResultHandler<Boolean>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Boolean>> = _savingState

    fun onExerciseNameChange(newName: String) {
        _name.value = newName
    }

    fun onExerciseDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onCategorySelected(newCategoryIndex: Int) {
        _category.value = allCategories[newCategoryIndex]
    }

    fun onImageChange(bitmap: Bitmap) {
        _image.value = ImageBitmap(ImageId.create(), exerciseId?.let {listOf(exerciseId!!.value)} ?: emptyList(), bitmap) // TODO image config
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
}
