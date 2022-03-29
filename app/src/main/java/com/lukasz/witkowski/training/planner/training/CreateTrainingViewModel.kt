package com.lukasz.witkowski.training.planner.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTrainingViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String>
        get() = _title

    fun onTrainingTitleChanged(newTitle: String) {
        _title.value = newTitle
    }

    private val _description = MutableStateFlow("")
    val description: StateFlow<String>
        get() = _description

    fun onTrainingDescriptionChanged(newDescription: String) {
        _description.value = newDescription
    }

    fun createTraining() {
        viewModelScope.launch {
            val trainingPlan = TrainingPlan(
                title = title.value,
                description = description.value,
                exercises = trainingExercises.value
            )
            trainingPlanService.saveTrainingPlan(trainingPlan)
        }
    }

    private val _trainingExercises = MutableStateFlow<List<TrainingExercise>>(emptyList())
    val trainingExercises: StateFlow<List<TrainingExercise>>
        get() = _trainingExercises

    private fun addTrainingExercise(exercise: TrainingExercise) {
        val mutableExercises = _trainingExercises.value.toMutableList()
        mutableExercises.add(exercise)
        _trainingExercises.value = mutableExercises.toList()
    }

    private val _pickedExercise = MutableStateFlow<com.lukasz.witkowski.training.planner.exercise.Exercise?>(null)
    val pickedExercise: StateFlow<com.lukasz.witkowski.training.planner.exercise.Exercise?> = _pickedExercise

    fun createTrainingExercise(
        exercise: com.lukasz.witkowski.training.planner.exercise.Exercise,
        reps: String,
        sets: String,
        minutes: Int,
        seconds: Int
    ) {
        val timeInMillis = TimeFormatter.timeToMillis(minutes = minutes, seconds = seconds)
        val trainingExercise = TrainingExercise(
            name = exercise.name,
            description = exercise.description,
//            category = exercise.category,
            repetitions = reps.toIntOrNull() ?: 1,
            sets = sets.toIntOrNull() ?: 1,
            time = timeInMillis
        )
        addTrainingExercise(trainingExercise)
    }

    fun removeTrainingExercise(trainingExercise: TrainingExercise) {
        val mutableExercises = trainingExercises.value.toMutableList()
        mutableExercises.remove(trainingExercise)
        _trainingExercises.value = mutableExercises
    }

    fun setRestTimeToExercise(
        exercise: TrainingExercise,
        restTimeMinutes: Int,
        restTimeSeconds: Int
    ) {
        val timeInMillis =
            TimeFormatter.timeToMillis(minutes = restTimeMinutes, seconds = restTimeSeconds)
        val exercises = trainingExercises.value.toMutableList()
        val index = exercises.indexOf(exercise)
        if (index >= 0) {
            exercises[index] =
                exercises[index].copy(restTime = timeInMillis) // TODO setting rest time does not work
        }
    }

    fun pickExercise(exercise: com.lukasz.witkowski.training.planner.exercise.Exercise) {
        _pickedExercise.value = exercise
    }
}