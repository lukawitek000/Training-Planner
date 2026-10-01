package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toRecommendation
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExerciseEditorViewModel(
    private val exerciseService: ExerciseService,
    private val categoryController: CategoryController2,
    private val exerciseId: ExerciseId? = null,
) : ViewModel() {
    val isEditMode = exerciseId != null
    private val initialState = exerciseId?.let { ExerciseEditingUiState.Loading(it) }
        ?: ExerciseEditingUiState.Editing(ExerciseEditingInput())
    private val filterCategories: StateFlow<List<FilterCategory>> =
        categoryController.filterCategories
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    val uiState: StateFlow<ExerciseEditingUiState>
        field = MutableStateFlow(initialState)

    init {
        viewModelScope.launch {
            filterCategories.collect { newCategories ->
                uiState.updateInput { it.copy(categories = newCategories) }
            }
        }
        exerciseId?.let { loadExercise(it) }
    }

    fun onEvent(event: ExerciseEditingEvent) {
        when (event) {
            is ExerciseEditingEvent.NameChanged -> uiState.updateInput { it.copy(name = event.name) }
            is ExerciseEditingEvent.DescriptionChanged -> {
                uiState.updateInput { it.copy(description = event.description) }
            }

            is ExerciseEditingEvent.CategoryToggled -> {
                categoryController.toggleCategory(event.category)
            }

            is ExerciseEditingEvent.ImageAdded -> {}
            is ExerciseEditingEvent.ImageRemoved -> {}
            is ExerciseEditingEvent.ImagePreviewChanged -> {}

            is ExerciseEditingEvent.RecommendedSetsChanged -> {
                uiState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(sets = event.sets)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedRepsChanged -> {
                uiState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(reps = event.reps)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedRestTimeChanged -> {
                uiState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(restTime = event.restTime)
                    }
                )
            }

            is ExerciseEditingEvent.RecommendedWeightChanged -> {
                uiState.updateRecommendationParam(
                    level = event.level,
                    updateParameters = { currentParams ->
                        currentParams.copy(weightInKg = event.weight)
                    }
                )
            }

            is ExerciseEditingEvent.SaveChangesRequested -> {
                saveExercise(input = event.input)
            }
        }
    }

    private fun MutableStateFlow<ExerciseEditingUiState>.updateInput(
        transform: (ExerciseEditingInput) -> ExerciseEditingInput,
    ) {
        update {
            if (it is ExerciseEditingUiState.Editing) {
                ExerciseEditingUiState.Editing(transform(it.exerciseEditingInput))
            } else {
                it
            }
        }
    }

    private fun MutableStateFlow<ExerciseEditingUiState>.updateRecommendationParam(
        level: RecommendationLevel,
        updateParameters: (RecommendedParameters) -> RecommendedParameters,
    ) {
        updateInput {
            val index = it.recommendations.indexOfFirst { it.level == level }
            if (index < 0) return@updateInput it
            val recommendation = it.recommendations[index]
            val params = updateParameters(recommendation.parameters)
            val newRecommendations = it.recommendations.toMutableList()
            newRecommendations[index] = recommendation.copy(parameters = params)
            it.copy(recommendations = newRecommendations)
        }
    }

    private fun saveExercise(input: ExerciseEditingInput) {
        viewModelScope.launch {
            runCatching {
                uiState.value = ExerciseEditingUiState.Saving(input)
                val exerciseConfiguration = input.toExerciseConfiguration()
                val exerciseId = saveExercise(exerciseConfiguration)
                uiState.value = ExerciseEditingUiState.Saved(exerciseId, exerciseConfiguration.name)
            }.onFailure {
                Timber.w("Failed to save exercise: ${it.message}")
                uiState.value =
                    ExerciseEditingUiState.Failure(input, it.message ?: "Uknown failure")
                delay(1.seconds)
                uiState.value = ExerciseEditingUiState.Editing(input)
            }
        }
    }

    private suspend fun saveExercise(config: ExerciseConfiguration): ExerciseId {
        return exerciseId?.let { exerciseService.updateExercise(config, it) }
            ?: exerciseService.saveExercise(config)
    }

    private fun loadExercise(exerciseId: ExerciseId) {
        viewModelScope.launch {
            runCatching {
                val exerciseDetails = exerciseService.getExerciseDetailsById(exerciseId).first()
                if (exerciseDetails != null) {
                    uiState.value = ExerciseEditingUiState.Editing(
                        exerciseDetails.toExerciseEditingState()
                    )
                    categoryController.selectCategories(exerciseDetails.exercise.categories.toSet())

                } else {
                    uiState.value = ExerciseEditingUiState.Failure(null, "Exercise not found")
                }
            }.onFailure {
                Timber.w("Failed to load exercise: ${it.message}")
                uiState.value =
                    ExerciseEditingUiState.Failure(null, it.message ?: "Unknown failure")
                delay(1.seconds)
                uiState.value = ExerciseEditingUiState.Editing(ExerciseEditingInput())
            }
        }
    }

    private fun ExerciseEditingInput.toExerciseConfiguration(): ExerciseConfiguration {
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

    private fun ExerciseDetails.toExerciseEditingState(): ExerciseEditingInput {
        return ExerciseEditingInput(
            name = exercise.name,
            description = exercise.description,
            categories = filterCategories.value,
            recommendations = listOf(
                beginnerRecommendation.toRecommendation(RecommendationLevel.BEGINNER),
                intermediateRecommendation.toRecommendation(RecommendationLevel.INTERMEDIATE),
                advancedRecommendation.toRecommendation(RecommendationLevel.ADVANCED),
            )
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
    data class RecommendedSetsChanged(val sets: Int?, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedRepsChanged(val reps: Int?, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedRestTimeChanged(val restTime: Duration?, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class RecommendedWeightChanged(val weight: Int?, val level: RecommendationLevel) :
        ExerciseEditingEvent

    data class SaveChangesRequested(val input: ExerciseEditingInput) : ExerciseEditingEvent
}

sealed interface ExerciseEditingUiState {
    data class Loading(
        val exerciseId: ExerciseId,
    ) : ExerciseEditingUiState

    data class Saving(val exerciseEditingInput: ExerciseEditingInput) :
        ExerciseEditingUiState

    data class Editing(val exerciseEditingInput: ExerciseEditingInput) :
        ExerciseEditingUiState

    data class Failure(
        val exerciseEditingInput: ExerciseEditingInput?,
        val message: String
    ) : ExerciseEditingUiState

    data class Saved(
        val exerciseId: ExerciseId,
        val name: String,
    ) : ExerciseEditingUiState
}

data class ExerciseEditingInput(
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
