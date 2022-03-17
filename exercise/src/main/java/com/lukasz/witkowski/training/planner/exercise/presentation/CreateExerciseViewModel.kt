package com.lukasz.witkowski.training.planner.exercise.presentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExerciseViewModel @Inject internal constructor(
    private val exerciseService: ExerciseService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savingState = MutableStateFlow<ResultHandler<Long>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Long>> = _savingState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    fun onExerciseNameChange(newName: String) {
        _title.value = newName
    }

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    fun onExerciseDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    private val _category = MutableStateFlow(Category())
    val category: StateFlow<Category> = _category

    fun onCategorySelected(newCategoryRes: Int) {
        _category.value = Category(newCategoryRes)
    }

    private val _image = MutableStateFlow<Bitmap?>(null)
    val image: StateFlow<Bitmap?> = _image

    fun onImageChange(bitmap: Bitmap) {
        _image.value = bitmap
    }

    val allCategories = CategoryMapper.allCategories

    fun createExercise() {
        viewModelScope.launch {
            _savingState.value = ResultHandler.Loading
            val exercise = Exercise(
                name = title.value ?: "",
                description = description.value ?: "",
                category = category.value,
                image = image.value
            )
            try {
                val exerciseId =
                    exerciseService.createExercise(ExerciseMapper.toDomainExercise(exercise)) // Long is not an id!!!
                _savingState.value =
                    ResultHandler.Success(0L) // TODO result handler requires long but exercise id was changed to String (UUID)
            } catch (e: Exception) {
                _savingState.value = ResultHandler.Error(message = "Saving exercise failed")
            }
        }
    }
}