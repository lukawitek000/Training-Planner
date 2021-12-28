package com.lukasz.witkowski.training.wearable.currentTraining.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.training.wearable.repo.CurrentTrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentTrainingProgressHelper {
    private lateinit var trainingWithExercises: TrainingWithExercises

    private var startTrainingTime = 0L

    private var currentExerciseIndex = 0
    private var currentSet = 1

    private val _currentTrainingState = MutableLiveData<CurrentTrainingState>()
    val currentTrainingState: LiveData<CurrentTrainingState> = _currentTrainingState

    var restTime = 0L
        private set
    var exerciseTime = 0L
        private set

    fun startTraining(trainingWithExercises: TrainingWithExercises) {
        this.trainingWithExercises = trainingWithExercises
        startTrainingTime = System.currentTimeMillis()
        _currentTrainingState.value = CurrentTrainingState.ExerciseState(trainingWithExercises.exercises[currentExerciseIndex])
    }

    fun navigateToTrainingExercise() {
        val nextExercise = getNextExercise()
        if (nextExercise != null) {
            _currentTrainingState.value = CurrentTrainingState.ExerciseState(nextExercise)
            restTime = nextExercise.restTime
            exerciseTime = nextExercise.time
        } else {
            navigateToTrainingSummary()
        }
    }

    fun navigateToTrainingRestTime() {
        if (restTime >= TimeFormatter.MILLIS_IN_SECONDS) {
            _currentTrainingState.value = CurrentTrainingState.RestTimeState(restTime)
        } else {
            navigateToTrainingExercise()
        }
    }

    fun navigateToTrainingSummary() {
        _currentTrainingState.value = CurrentTrainingState.SummaryState
    }

    private fun getNextExercise() : TrainingExercise? {
        val exercises = trainingWithExercises.exercises

        while (currentExerciseIndex + 1 < exercises.size) {
            currentExerciseIndex = if (currentExerciseIndex + 1 >= exercises.size) {
                break
            } else {
                currentExerciseIndex + 1
            }
            if (exercises[currentExerciseIndex].sets >= currentSet) {
                return exercises[currentExerciseIndex]
            }
        }
        return exercises.firstOrNull {
            it.sets >= currentSet + 1
        }
    }

}