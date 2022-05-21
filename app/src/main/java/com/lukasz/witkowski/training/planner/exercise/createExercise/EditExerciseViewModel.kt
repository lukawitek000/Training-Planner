package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : CreateExerciseViewModel(exerciseService, categoriesCollection, savedStateHandle) {

    private val _exerciseId = savedStateHandle.get<String>("exerciseId")!!
    private val exerciseId: ExerciseId
        get() = ExerciseId(_exerciseId)


    init {
        viewModelScope.launch {
            exerciseService.getExerciseById(exerciseId).collect { domainExercise ->
                val exercise = ExerciseMapper.toPresentationExercise(domainExercise)
                onExerciseNameChange(exercise.name)
                onExerciseDescriptionChange(exercise.description)
                val index = allCategories.indexOf(exercise.category)
                if (index >= 0) {
                    onCategorySelected(index)
                }
                exercise.image?.let {
                    onImageChange(it)
                }
            }
        }
    }

    override fun createExercise() {
        viewModelScope.launch {
            val exercise = Exercise(
                id = exerciseId,
                name = name.value,
                description = description.value,
                category = category.value,
                image = image.value
            )
            updateExercise(exercise)
        }
    }

    private suspend fun updateExercise(exercise: Exercise) {
        try {
            _savingState.value = ResultHandler.Loading
            val isUpdateSuccessful =
                exerciseService.updateExercise(ExerciseMapper.toDomainExercise(exercise))
            if(!isUpdateSuccessful) throw Exception("Updating exercise has failed")
            _savingState.value =
                ResultHandler.Success(isUpdateSuccessful)
        } catch (e: Exception) {
            _savingState.value = ResultHandler.Error(message = "Updating exercise has failed")
        }
    }
}