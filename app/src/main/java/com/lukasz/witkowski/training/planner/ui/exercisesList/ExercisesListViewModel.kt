package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedCategories = MutableLiveData<List<Category>>(emptyList())
    val selectedCategories: LiveData<List<Category>> = _selectedCategories

    val exercises: LiveData<List<Exercise>> = Transformations.switchMap(_selectedCategories) {
        exerciseRepository.loadExercises(it)
    }

    fun selectCategory(category: Category) {
        val list = _selectedCategories.value?.toMutableList()
        if (list == null) {
            _selectedCategories.value = listOf(category)
            return
        }
        if (list.contains(category)) {
            list.remove(category)
        } else {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
    }
}
