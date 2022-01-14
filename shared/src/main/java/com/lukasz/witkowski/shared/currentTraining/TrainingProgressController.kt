package com.lukasz.witkowski.shared.currentTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TimeFormatter
import timber.log.Timber

class TrainingProgressController {

    private lateinit var trainingWithExercises: TrainingWithExercises

    private val trainingId: Long
        get() = trainingWithExercises.training.id

    private val _currentTrainingState = MutableLiveData<CurrentTrainingState>()
    val currentTrainingState: LiveData<CurrentTrainingState> = _currentTrainingState


    private var currentExerciseIndex = 0
    var currentSet = 1
        private set

    var restTime = 0L
        private set

    var exerciseTime = 0L
        private set

    private var startTrainingTime = 0L
    val trainingTime: Long
        get() = System.currentTimeMillis() - startTrainingTime

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
        if (exercise.time <= TimeFormatter.MILLIS_IN_SECOND) 0L else exercise.time

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
        Timber.d("Next exercise $nextExercise")
        if (nextExercise != null) {
            restTime = setRestTime(nextExercise)
            exerciseTime = setExerciseTime(nextExercise)
            _currentTrainingState.value = CurrentTrainingState.ExerciseState(nextExercise)
        } else {
            navigateToTrainingSummary()
        }
    }

    private fun setRestTime(exercise: TrainingExercise): Long {
        return if (exercise.restTime <= TimeFormatter.MILLIS_IN_SECOND) 0L else exercise.restTime
    }

    fun navigateToTrainingRestTime() {
        if (restTime > TimeFormatter.MILLIS_IN_SECOND && !isLastExercise()) {
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

    private fun getNextExercise(): TrainingExercise? {
        val exercises = trainingWithExercises.exercises
        currentExerciseIndex = getNextExerciseIndexFromCurrentSet(exercises)
        if (currentExerciseIndex != -1) return exercises[currentExerciseIndex]
        currentSet++
        currentExerciseIndex = exercises.indexOfFirst { it.sets >= currentSet }
        return exercises.firstOrNull {
            it.sets >= currentSet
        }
    }

    private fun getNextExerciseIndexFromCurrentSet(exercises: List<TrainingExercise>): Int {
        var idx = currentExerciseIndex
        while (idx + 1 < exercises.size) {
            idx++
            if (exercises[idx].sets >= currentSet) {
                return idx
            }
        }
        return -1
    }

    fun isRestTimeNext(): Boolean {
        val state =
            currentTrainingState.value as? CurrentTrainingState.ExerciseState ?: return false
        return state.exercise.restTime > TimeFormatter.MILLIS_IN_SECOND
    }

    val isExerciseState: Boolean
        get() {
            return _currentTrainingState.value is CurrentTrainingState.ExerciseState
        }

    val isRestTimeState: Boolean
        get() {
            return _currentTrainingState.value is CurrentTrainingState.RestTimeState
        }

    fun isLastExercise(): Boolean {
        val exercises = trainingWithExercises.exercises
        if (getNextExerciseIndexFromCurrentSet(exercises) != -1) return false
        val nextSet = currentSet + 1
        return !exercises.any {
            it.sets >= nextSet
        }
    }
}