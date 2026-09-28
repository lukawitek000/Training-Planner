package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExerciseEditorViewModel(
    private val exerciseService: ExerciseService,
    private val categoryController: CategoryController2,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val editingState = MutableStateFlow(ExerciseEditingState())
    private val operatingMode = MutableStateFlow<OperatingMode>(OperatingMode.Editing)

    val uiState = combine(
        editingState,
        operatingMode,
        categoryController.filterCategories
    ) { editingState, mode, categories ->
        when (mode) {
            OperatingMode.Editing -> ExerciseEditingUiState.Editing(
                editingState.copy(categories = categories)
            )

            OperatingMode.Saving -> ExerciseEditingUiState.Saving(
                editingState.copy(categories = categories)
            )
            is OperatingMode.Failure ->  ExerciseEditingUiState.Failure(
                editingState.copy(categories = categories),
                mode.message
            )
            is OperatingMode.Saved -> ExerciseEditingUiState.Saved(mode.exerciseId)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        initialValue = ExerciseEditingUiState.Editing(editingState.value)
    )

    init {
        viewModelScope.launch {
            categoryController.filterCategories.collectLatest { newCategories ->
                editingState.update { it.copy(categories = newCategories) }
            }
        }
    }

    fun onEvent(event: ExerciseEditingEvent) {
        when (event) {
            is ExerciseEditingEvent.NameChanged -> editingState.update { it.copy(name = event.name) }
            is ExerciseEditingEvent.DescriptionChanged -> {
                editingState.update { it.copy(description = event.description) }
            }

            is ExerciseEditingEvent.CategoryToggled -> {
                categoryController.toggleCategory(event.category)
            }

            is ExerciseEditingEvent.ImageAdded -> {}
            is ExerciseEditingEvent.ImageRemoved -> {}
            is ExerciseEditingEvent.ImagePreviewChanged -> {}

            is ExerciseEditingEvent.RecommendedSetsChanged -> {
                editingState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(sets = event.sets)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedRepsChanged -> {
                editingState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(reps = event.reps)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedRestTimeChanged -> {
                editingState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(restTime = event.restTime)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedWeightChanged -> {
                editingState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(weightInKg = event.weight)
                    }
                )
            }

            is ExerciseEditingEvent.CreateExercise -> {
                saveExercise()
            }
        }
    }

    private fun MutableStateFlow<ExerciseEditingState>.updateRecommendationParam(
        level: RecommendationLevel,
        updateParameters: (RecommendedParameters) -> RecommendedParameters,
    ) {
        update {
            val index = it.recommendations.indexOfFirst { it.level == level }
            if (index < 0) return@update it
            val recommendation = it.recommendations[index]
            val params = updateParameters(recommendation.parameters)
            val newRecommendations = it.recommendations.toMutableList()
            newRecommendations[index] = recommendation.copy(parameters = params)
            it.copy(recommendations = newRecommendations)
        }
    }

    private fun saveExercise() {
        viewModelScope.launch {
            runCatching {
                operatingMode.value = OperatingMode.Saving
                val exerciseId = exerciseService.saveExercise(
                    exerciseConfiguration = editingState.value.toExerciseConfiguration()
                )
                operatingMode.value = OperatingMode.Saved(exerciseId)
            }.onFailure {
                Timber.w("Failed to save exercise: ${it.message}")
                operatingMode.value = OperatingMode.Failure(it.message ?: "Uknown failure")
                delay(1.seconds)
                operatingMode.value = OperatingMode.Editing
            }
        }
    }

    private fun ExerciseEditingState.toExerciseConfiguration(): ExerciseConfiguration {
        return ExerciseConfiguration(
            name = name,
            description = description,
            categories = categories.filter { it.isSelected }.map { it.category },
            image = null,
            beginnerRecommendation = recommendations.toExerciseRecommendation(RecommendationLevel.BEGINNER),
            intermediateRecommendation = recommendations.toExerciseRecommendation(
                RecommendationLevel.INTERMEDIATE
            ),
            advancedRecommendation = recommendations.toExerciseRecommendation(RecommendationLevel.ADVANCED),
        )
    }
}

sealed interface ExerciseEditingEvent {
    data class NameChanged(val name: String) : ExerciseEditingEvent
    data class DescriptionChanged(val description: String) : ExerciseEditingEvent
    data class CategoryToggled(val category: ExerciseCategory) : ExerciseEditingEvent
    data class ImageAdded(val bitmap: ImageBitmap) : ExerciseEditingEvent
    data class ImageRemoved(val bitmap: ImageBitmap) : ExerciseEditingEvent
    data class ImagePreviewChanged(val bitmap: ImageBitmap) : ExerciseEditingEvent
    data class RecommendedSetsChanged(val sets: Int, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedRepsChanged(val reps: Int, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedRestTimeChanged(val restTime: Duration, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedWeightChanged(val weight: Int, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data object CreateExercise : ExerciseEditingEvent
}

sealed interface ExerciseEditingUiState {
    data class Saving(val exerciseEditingState: ExerciseEditingState) :
        ExerciseEditingUiState

    data class Editing(val exerciseEditingState: ExerciseEditingState) :
        ExerciseEditingUiState

    data class Failure(
        val exerciseEditingState: ExerciseEditingState,
        val message: String
    ) : ExerciseEditingUiState

    data class Saved(
        val exerciseId: ExerciseId,
    ): ExerciseEditingUiState
}

private sealed interface OperatingMode {
    data object Editing : OperatingMode
    data object Saving : OperatingMode
    data class Failure(val message: String) : OperatingMode
    data class Saved(val exerciseId: ExerciseId): OperatingMode
}

data class ExerciseEditingState(
    val name: String = "",
    val description: String = "",
    val categories: List<FilterCategory> = emptyList(),
    val images: List<ImageBitmap> = emptyList(),
    val previewImage: ImageBitmap? = null,
    val recommendations: List<Recommendation> = listOf(
        Recommendation(RecommendationLevel.BEGINNER),
        Recommendation(RecommendationLevel.INTERMEDIATE),
        Recommendation(RecommendationLevel.ADVANCED),
    ),
) {
    fun isValidForCreation(): Boolean {
        return name.isNotEmpty() && categories.any { it.isSelected } && recommendations.all { it.parameters.areValid() }
    }
}
