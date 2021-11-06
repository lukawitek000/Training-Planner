package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.training.planner.repository.ExerciseRepository
import com.lukasz.witkowski.training.planner.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateTrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    init {
        Timber.d("Iniitttt")
    }

    private val _title = MutableStateFlow("")
    val title: StateFlow<String>
        get() = _title

    fun onTrainingTitleChanged(newTitle: String){
        _title.value = newTitle
    }

    private val _description = MutableStateFlow("")
    val description: StateFlow<String>
        get() = _description

    fun onTrainingDescriptionChanged(newDescription: String){
        _description.value = newDescription
    }

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>>
        get() = _exercises

    fun addPickedExercises(pickedExercises: List<Exercise>) {
        Timber.d("Added picked exercises $pickedExercises")
        val mutableExercises = exercises.value.toMutableList()
        mutableExercises.addAll(pickedExercises)
        _exercises.value = mutableExercises.toList()
        Timber.d("Training exercises ${exercises.value}")
    }

    fun createTraining() {
        // create training in database
        val training = Training(
            title = title.value,
            description = description.value,
            exercises = exercises.value
        )
        Timber.d("Create training $training")
       // cleanData()
    }

    private fun cleanData() {
        _title.value = ""
        _description.value = ""
        _exercises.value = emptyList()
        // TODO Find better way than holding instance of this viewmodel in Navigation.kt, it will be kept for app live
    }

}