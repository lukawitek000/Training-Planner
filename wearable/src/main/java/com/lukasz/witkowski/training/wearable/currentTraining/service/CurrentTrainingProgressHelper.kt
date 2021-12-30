package com.lukasz.witkowski.training.wearable.currentTraining.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.currentTraining.CurrentTrainingState
import timber.log.Timber

object CurrentTrainingProgressHelper {
    private lateinit var trainingWithExercises: TrainingWithExercises

    private var startTrainingTime = 0L

    private var currentExerciseIndex = 0
    private var currentSet = 1
    private val trainingId: Long
        get() = trainingWithExercises.training.id

    private val _currentTrainingState = MutableLiveData<CurrentTrainingState>()
    val currentTrainingState: LiveData<CurrentTrainingState> = _currentTrainingState

    val isExerciseState: Boolean
        get() {
            return currentTrainingState.value is CurrentTrainingState.ExerciseState
        }

    val isRestTimeState: Boolean
        get() {
            return currentTrainingState.value is CurrentTrainingState.RestTimeState
        }

    var restTime = 0L
        private set
    var exerciseTime = 0L
        private set

    fun startTraining(trainingWithExercises: TrainingWithExercises) {
        this.trainingWithExercises = trainingWithExercises
        startTrainingTime = System.currentTimeMillis()
        if (_currentTrainingState.value == null || _currentTrainingState.value is CurrentTrainingState.SummaryState || isDifferentTrainingRunning()) {
            val exercise = trainingWithExercises.exercises[currentExerciseIndex]
            restTime = setRestTime(exercise)
            exerciseTime = setExerciseTime(exercise)
            _currentTrainingState.value =
                CurrentTrainingState.ExerciseState(exercise)
        }
    }

    private fun setExerciseTime(exercise: TrainingExercise) =
        if (exercise.time < TimeFormatter.MILLIS_IN_SECONDS) 0L else exercise.time

    private fun isDifferentTrainingRunning(): Boolean {
        val state = _currentTrainingState.value ?: return true
        return when (state) {
            is CurrentTrainingState.ExerciseState -> state.exercise.trainingId != trainingId
            is CurrentTrainingState.RestTimeState -> state.trainingId != trainingId
            is CurrentTrainingState.SummaryState -> true
        }
    }

    fun navigateToTrainingExercise() {
        val nextExercise = getNextExercise()
        if (nextExercise != null) {
            restTime = setRestTime(nextExercise)
            exerciseTime = setExerciseTime(nextExercise)
            _currentTrainingState.value = CurrentTrainingState.ExerciseState(nextExercise)
        } else {
            navigateToTrainingSummary()
        }
    }

    private fun setRestTime(exercise: TrainingExercise): Long {
       return if(exercise.restTime < TimeFormatter.MILLIS_IN_SECONDS) 0L else exercise.restTime
    }

    fun navigateToTrainingRestTime() {
        if (restTime >= TimeFormatter.MILLIS_IN_SECONDS) {
            _currentTrainingState.value = CurrentTrainingState.RestTimeState(restTime, trainingId)
        } else {
            navigateToTrainingExercise()
        }
    }

    private fun navigateToTrainingSummary() {
        resetData()
        _currentTrainingState.value = CurrentTrainingState.SummaryState
    }

    fun resetData() {
        currentExerciseIndex = 0
        currentSet = 1
    }

    private fun getNextExercise() : TrainingExercise? {
        val exercises = trainingWithExercises.exercises
        while (currentExerciseIndex + 1 < exercises.size) {
            currentExerciseIndex++
            if (exercises[currentExerciseIndex].sets >= currentSet) {
                return exercises[currentExerciseIndex]
            }
        }
        currentSet++
        currentExerciseIndex = 0
        return exercises.firstOrNull {
            it.sets >= currentSet
        }
    }
}
