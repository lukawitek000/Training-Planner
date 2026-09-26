package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExerciseEditorViewModel(
    private val exerciseService: ExerciseService,
    categoryController: CategoryController2,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val editingState: StateFlow<ExerciseEditingState>
        field = MutableStateFlow(ExerciseEditingState())

    fun onEvent(event: ExerciseEditingEvent) {

    }
}

sealed interface ExerciseEditingEvent {
    data class NameChanged(val name: String) : ExerciseEditingEvent
    data class DescriptionChanged(val description: String) : ExerciseEditingEvent
    data class CategoriesChanged(val category: ExerciseCategory) : ExerciseEditingEvent
    data class ImageAdded(val bitmap: ImageBitmap) : ExerciseEditingEvent
    data class ImageRemoved(val bitmap: ImageBitmap) : ExerciseEditingEvent
    data class ImagePreviewChanged(val bitmap: ImageBitmap) : ExerciseEditingEvent
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
)

enum class RecommendationLevel(val nameRes: Int, val color: Color) {
    BEGINNER(R.string.beginner, Color.Green),
    INTERMEDIATE(R.string.intermediate, Color.Yellow),
    ADVANCED(R.string.advanced, Color.Red),
}

data class Recommendation(
    val level: RecommendationLevel,
    val parameters: RecommendedParameters? = null
)

data class RecommendedParameters(
    val level: RecommendationLevel,
    val sets: Int = 1,
    val reps: Int = 1,
    val restTime: Duration = 60.seconds,
    val weightInKg: Int? = null
)
