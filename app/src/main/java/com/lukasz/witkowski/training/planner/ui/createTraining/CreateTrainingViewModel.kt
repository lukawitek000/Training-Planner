package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.TimeFormatter.MILLIS_IN_SECONDS
import com.lukasz.witkowski.shared.utils.TimeFormatter.SECONDS_IN_MINUTE
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateTrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
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
            val training = Training(
                title = title.value,
                description = description.value
            )
            val trainingWithExercises = TrainingWithExercises(
                training = training,
                exercises = trainingExercises.value
            )
            trainingRepository.insertTrainingWithExercises(trainingWithExercises)
            Timber.d("Create training $trainingWithExercises")
        }
    }

    private val _trainingExercises = MutableStateFlow<List<TrainingExercise>>(emptyList())
    val trainingExercises: StateFlow<List<TrainingExercise>>
        get() = _trainingExercises

    private fun addTrainingExercise(trainingExercise: TrainingExercise) {
        val mutableExercises = _trainingExercises.value.toMutableList()
        mutableExercises.add(trainingExercise)
        _trainingExercises.value = mutableExercises.toList()
    }

    fun createTrainingExercise(
        exercise: Exercise,
        reps: String,
        sets: String,
        minutes: Int,
        seconds: Int
    ) {
        val timeInMillis = (minutes * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECONDS
        val repetitions = reps.toIntOrNull() ?: 1
        val sets = sets.toIntOrNull() ?: 1
        val trainingExercise = TrainingExercise(
            exercise = exercise,
            repetitions = repetitions,
            sets = sets,
            time = timeInMillis.toLong()
        )
        addTrainingExercise(trainingExercise)
    }

    fun removeTrainingExercise(trainingExercise: TrainingExercise) {
        val mutableExercises = trainingExercises.value.toMutableList()
        mutableExercises.remove(trainingExercise)
        _trainingExercises.value = mutableExercises
    }
}