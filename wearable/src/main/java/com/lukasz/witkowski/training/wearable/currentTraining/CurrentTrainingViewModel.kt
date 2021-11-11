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

    private var restTime = 0L
    private val _currentRestTime = MutableLiveData<Long>(0L)
    val currentRestTime : LiveData<Long>
        get() = _currentRestTime

    var isExerciseTimerRunning = false

    fun navigateToTrainingExercise() {
        _currentTrainingState.value = CurrentTrainingState.ExerciseState
        cancelRestTimer()
        getNextExercise()
    }

    fun navigateToTrainingRestTime() {
        _currentTrainingState.value = CurrentTrainingState.RestTimeState
        startRestTimer()
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
        setNewRestTime()
    }

    private fun setNewRestTime() {
        restTime = _currentExercise.value!!.restTime
        _currentRestTime.value = restTime
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



    private var timer : CountDownTimer? = null

    fun startRestTimer() {
        timer = object : CountDownTimer(restTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.d("Tick $millisUntilFinished")
                _currentRestTime.value = millisUntilFinished
            }

            override fun onFinish() {
                Timber.d("Finished")
                timer = null
                navigateToTrainingExercise()
            }

        }.start()
    }


    private fun cancelRestTimer() {
        timer?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelRestTimer()
    }


}