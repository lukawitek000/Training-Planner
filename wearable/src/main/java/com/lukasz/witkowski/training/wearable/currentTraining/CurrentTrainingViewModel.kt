package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.repo.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingRepository: TrainingRepository
    ) : ViewModel() {

    private val _currentTrainingState =
        MutableLiveData<CurrentTrainingState>(CurrentTrainingState.ExerciseState)
    val currentTrainingState: LiveData<CurrentTrainingState>
        get() = _currentTrainingState

    private var trainingWithExercises: TrainingWithExercises? = null
    private val _currentExercise = MutableLiveData<TrainingExercise>()
    val currentExercise : LiveData<TrainingExercise>
        get() = _currentExercise
    private var currentExerciseIndex = 0
    var exerciseTime = 0L
        private set

    var restTime = 0L
        private set

    fun navigateToTrainingExercise() {
        _currentTrainingState.value = CurrentTrainingState.ExerciseState
        getNextExercise()
    }

    fun navigateToTrainingRestTime() {
        _currentTrainingState.value = CurrentTrainingState.RestTimeState
    }

    fun navigateToTrainingSummary() {
        _currentTrainingState.value = CurrentTrainingState.SummaryState
    }

    fun fetchTraining(trainingId: Long) {
        viewModelScope.launch {
            trainingWithExercises = trainingRepository.fetchTrainingById(trainingId)
            currentExerciseIndex = 0
            setNewCurrentExercise()
        }
    }

    private fun setNewCurrentExercise() {
        _currentExercise.value = trainingWithExercises!!.exercises[currentExerciseIndex]
        exerciseTime = _currentExercise.value?.time ?: 0L
        restTime = _currentExercise.value?.restTime ?: 0L
    }

    private fun getNextExercise() {
        val nextExerciseIndex = currentExerciseIndex + 1
        if (nextExerciseIndex >= trainingWithExercises!!.exercises.size){
            currentExerciseIndex = 0
        } else {
            currentExerciseIndex++
        }
        setNewCurrentExercise()
    }
}