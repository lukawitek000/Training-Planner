package com.lukasz.witkowski.training.wearable.repo

import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CurrentTrainingRepository {

    lateinit var trainingWithExercises: TrainingWithExercises

    private val _currentExercise = MutableStateFlow(Exercise())
    val currentExercise: StateFlow<Exercise> = _currentExercise


}