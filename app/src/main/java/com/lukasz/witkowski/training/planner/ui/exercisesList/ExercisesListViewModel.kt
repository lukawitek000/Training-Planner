package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.dummyExerciseList
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _exercises = MutableLiveData(emptyList<Exercise>())
    val exercises: LiveData<List<Exercise>> = exerciseRepository.loadAllExercises()

//    init {
//        loadAllExercises()
//    }
//
//    fun loadAllExercises() {
//        viewModelScope.launch {
//            _exercises.value = exerciseRepository.loadAllExercises()
//        }
//    }

}