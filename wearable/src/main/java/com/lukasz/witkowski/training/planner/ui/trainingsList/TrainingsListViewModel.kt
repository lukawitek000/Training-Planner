package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingRepository: TrainingRepository
) : ViewModel() {


    private val _trainings = MutableLiveData<ResultHandler<List<TrainingWithExercises>>>()
    val trainings: LiveData<ResultHandler<List<TrainingWithExercises>>> = _trainings

    fun getTrainingsWithExercises() {
        viewModelScope.launch {
            _trainings.value = ResultHandler.Loading
            trainingRepository.getAllTrainingsWithExercises().collect {
                _trainings.value = ResultHandler.Success(it)
//                _trainings.value = ResultHandler.Success(trainingRepository.getDummyTrainings())
            }
        }
    }
}