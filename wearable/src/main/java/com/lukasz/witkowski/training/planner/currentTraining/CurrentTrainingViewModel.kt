package com.lukasz.witkowski.training.planner.currentTraining

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
class CurrentTrainingViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _trainingWithExercises = MutableLiveData<ResultHandler<TrainingWithExercises>>()
    val trainingWithExercises: LiveData<ResultHandler<TrainingWithExercises>> = _trainingWithExercises

    fun fetchTraining(trainingId: Long) {
        viewModelScope.launch {
            _trainingWithExercises.value = ResultHandler.Loading
//            _trainingWithExercises.value = ResultHandler.Success(trainingRepository.fetchDummyTrainingById(trainingId))
            _trainingWithExercises.value = ResultHandler.Success(trainingRepository.fetchTrainingById(trainingId))
        }
    }
}
