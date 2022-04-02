package com.lukasz.witkowski.training.planner.training.createTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.models.Exercise
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.models.TrainingPlan
import com.lukasz.witkowski.training.planner.training.models.TrainingPlanMapper
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

    private val _description = MutableStateFlow("")
    val description: StateFlow<String>
        get() = _description

    private val _trainingExercises = MutableStateFlow<List<TrainingExercise>>(mutableListOf())
    val trainingExercises: StateFlow<List<TrainingExercise>>
        get() = _trainingExercises

    private val _pickedExercise = MutableStateFlow<Exercise?>(null)
    val pickedExercise: StateFlow<Exercise?> = _pickedExercise

    val pickedExercisesIds =
        mutableListOf<ExerciseId>() // TODO how to handle marking exercises that are already in the training plan
    // Maybe TrainingTraining class should have Exercise property plus required properties for training plan such as sets reps etc.

    fun onTrainingTitleChanged(newTitle: String) {
        _title.value = newTitle
    }

    fun onTrainingDescriptionChanged(newDescription: String) {
        _description.value = newDescription
    }

    private fun addTrainingExercise(trainingExercise: TrainingExercise, exerciseId: ExerciseId) {
        _trainingExercises.value += trainingExercise
        pickedExercisesIds.add(exerciseId)
    }

    fun removeTrainingExercise(trainingExercise: TrainingExercise) {
        val index = _trainingExercises.value.indexOf(trainingExercise)
        _trainingExercises.value -= trainingExercise
        pickedExercisesIds.removeAt(index)
    }

    fun createTrainingExercise(
        exercise: Exercise,
        reps: String,
        sets: String,
        minutes: Int,
        seconds: Int
    ) {
        val timeInMillis = TimeFormatter.timeToMillis(minutes = minutes, seconds = seconds)
        val trainingExercise = TrainingExercise(
            id = TrainingExerciseId.create(),
            name = exercise.name,
            description = exercise.description,
            category = exercise.category,
            repetitions = reps.toIntOrNull() ?: 1,
            sets = sets.toIntOrNull() ?: 1,
            time = timeInMillis
        )
        addTrainingExercise(trainingExercise, exercise.id)
    }

    fun setRestTimeToExercise(
        exercise: TrainingExercise,
        restTimeMinutes: Int,
        restTimeSeconds: Int
    ) {
        val timeInMillis =
            TimeFormatter.timeToMillis(minutes = restTimeMinutes, seconds = restTimeSeconds)
        val updatedExercise = exercise.copy(restTime = timeInMillis)
        val exercisesList = _trainingExercises.value.toMutableList()
        val index = exercisesList.indexOf(exercise)
        exercisesList[index] = updatedExercise
        _trainingExercises.value = exercisesList.toList()
    }

    /**
     * Returns true if the exercise is already used in the training plan
     */
    fun pickExercise(exercise: Exercise): Boolean {
        _pickedExercise.value = exercise
        return pickedExercisesIds.contains(exercise.id)
    }

    fun createTrainingPlan() {
        viewModelScope.launch {
            val trainingPlan = TrainingPlan(
                id = TrainingPlanId.create(),
                title = title.value,
                description = description.value,
                exercises = trainingExercises.value
            )
            trainingPlanService.saveTrainingPlan(
                TrainingPlanMapper.toDomainTrainingPlan(
                    trainingPlan
                )
            )
        }
    }
}
