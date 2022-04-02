package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/** StateFlow with SavedStateHandle, do I need it?
 * https://medium.com/mobile-app-development-publication/saving-stateflow-state-in-viewmodel-2ee9ed9b1a83
 */

@HiltViewModel
class CreateExerciseViewModel @Inject internal constructor(
    private val exerciseService: ExerciseService,
    private val categoryService: CategoryService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _category = MutableStateFlow(Category())
    val category: StateFlow<Category> = _category

    private val _image = MutableStateFlow<Bitmap?>(null)
    val image: StateFlow<Bitmap?> = _image

    private val _savingState = MutableStateFlow<ResultHandler<Boolean>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Boolean>> = _savingState

    fun onExerciseNameChange(newName: String) {
        _name.value = newName
    }

    fun onExerciseDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onCategorySelected(newCategoryIndex: Int) {
        Timber.d("Category selected index $newCategoryIndex")
        _category.value = getAllCategories()[newCategoryIndex]
    }

    fun onImageChange(bitmap: Bitmap) {
        _image.value = bitmap
    }

    fun getAllCategories(): List<Category> {
        return categoryService.getAllCategories().map { CategoryMapper.toCategory(it) }
    }

    fun createExercise() {
        viewModelScope.launch {
            val exercise = Exercise(
                id = ExerciseId.create(),
                name = name.value,
                description = description.value,
                category = category.value,
                image = image.value
            )
            saveExercise(exercise)
        }
    }

    private suspend fun saveExercise(exercise: Exercise) {
        try {
            _savingState.value = ResultHandler.Loading
            val isSavingFinished =
                exerciseService.saveExercise(ExerciseMapper.toDomainExercise(exercise))
            _savingState.value =
                ResultHandler.Success(isSavingFinished)
        } catch (e: Exception) {
            _savingState.value = ResultHandler.Error(message = "Saving exercise failed")
        }
    }
}