package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExerciseEditorViewModel(
    private val exerciseService: ExerciseService,
    private val categoryController: CategoryController2,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val editingState: StateFlow<ExerciseEditingState>
        field = MutableStateFlow(ExerciseEditingState())

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
                editingState.update { it.copy(name = event.description) }
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

enum class RecommendationLevel(val nameRes: Int, val color: Color) {
    BEGINNER(R.string.beginner, Color.Green),
    INTERMEDIATE(R.string.intermediate, Color.Yellow),
    ADVANCED(R.string.advanced, Color.Red),
}

data class Recommendation(
    val level: RecommendationLevel,
    val parameters: RecommendedParameters = RecommendedParameters()
)

data class RecommendedParameters(
    val sets: Int? = null,
    val reps: Int? = null,
    val restTime: Duration? = null,
    val weightInKg: Int? = null
) {
    fun areValid() = sets != null && reps != null && restTime != null
}
