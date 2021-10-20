package com.lukasz.witkowski.training.planner.ui.createExercise

import androidx.lifecycle.*
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.dummyExerciseList
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {


    private var exercise: Exercise? = null

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
        _category.value = Category.allCategories.firstOrNull {
            it.name == newCategory
        } ?: Category.None
    }


    fun createExercise(){
        viewModelScope.launch {
            val exercise = Exercise(
                name = title.value ?: "",
                description = description.value ?: "",
                category = category.value ?: Category.None
            )
//            dummyExerciseList.add(exercise)
            exerciseRepository.insertExercise(exercise)
        }

    }

}