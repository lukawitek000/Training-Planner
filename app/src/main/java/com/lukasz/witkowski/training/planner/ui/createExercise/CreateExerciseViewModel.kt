package com.lukasz.witkowski.training.planner.ui.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.allCategories
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _savingState = MutableStateFlow<ResultHandler<Long>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Long>> = _savingState

    private val _title = MutableLiveData("")
    val title: LiveData<String> = _title

    fun onExerciseNameChange(newName: String){
        _title.value = newName
    }

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    fun onExerciseDescriptionChange(newDescription: String){
        _description.value = newDescription
    }

    private val _category = MutableLiveData<Category>(Category.None)
    val category: LiveData<Category> = _category

    fun onCategorySelected(newCategory: String) {
        _category.value = allCategories.firstOrNull {
            it.name == newCategory
        } ?: Category.None
    }

    private val _image = MutableLiveData<Bitmap?>(null)
    val image: LiveData<Bitmap?> = _image

    fun onImageChange(bitmap: Bitmap){
        _image.value = bitmap
    }
    fun createExercise(){
        viewModelScope.launch {
            _savingState.value = ResultHandler.Loading
            val exercise = Exercise(
                name = title.value ?: "",
                description = description.value ?: "",
                category = category.value ?: Category.None,
                image = image.value
            )
            try {

                val exerciseId = exerciseRepository.insertExercise(exercise)
                _savingState.value = ResultHandler.Success(exerciseId)
            } catch (e: Exception) {
                _savingState.value = ResultHandler.Error(message = "Saving exercise failed")
            }
        }
    }
}