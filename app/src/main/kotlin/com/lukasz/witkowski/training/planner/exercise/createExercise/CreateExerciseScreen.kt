package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.OverlayLoading
import com.lukasz.witkowski.training.planner.ui.components.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CreateExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseEditorViewModel,
    navigateToDetails: (ExerciseId) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    CreateExerciseScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = state,
        onUserInputChange = viewModel::onEvent,
        onExerciseSaved = navigateToDetails
    )
}

@Composable
private fun CreateExerciseScreenContent(
    uiState: ExerciseEditingUiState,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    onExerciseSaved: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when (uiState) {
        is ExerciseEditingUiState.Saving -> OverlayLoading(Modifier.fillMaxSize()) {
            ExerciseForm(
                state = uiState.exerciseEditingState,
                modifier = modifier,
                onUserInputChange = onUserInputChange
            )
        }
        is ExerciseEditingUiState.Editing -> ExerciseForm(
            state = uiState.exerciseEditingState,
            modifier = modifier,
            onUserInputChange = onUserInputChange
        )
        is ExerciseEditingUiState.Failure -> {
            LaunchedEffect(uiState) {
                Toast.makeText(context, "Failed exercise save: ${uiState.message}", Toast.LENGTH_SHORT).show()
            }
            ExerciseForm(
                state = uiState.exerciseEditingState,
                modifier = modifier,
                onUserInputChange = onUserInputChange
            )
        }
        is ExerciseEditingUiState.Saved -> LaunchedEffect(uiState) {
            onExerciseSaved(uiState.exerciseId)
        }
    }
}

@Composable
private fun ExerciseForm(
    state: ExerciseEditingState,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(Dimens.normal)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        ImageInputForm(
            images = state.images,
            previewImage = state.previewImage
        )
        TextInputForm(
            name = state.name,
            description = state.description,
            onUserInputChange = onUserInputChange,
        )
        CategorySelectionPanel(
            categories = state.categories,
            toggleCategory = { onUserInputChange(ExerciseEditingEvent.CategoryToggled(it)) },
        )
        RecommendedParametersForm(
            recommendations = state.recommendations,
            onRecommendationChange = onUserInputChange
        )
        Button(
            onClick = { onUserInputChange(ExerciseEditingEvent.CreateExercise) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isValidForCreation()
        ) {
            Text(stringResource(R.string.create_exercise))
        }
    }
}

@Composable
private fun TextInputForm(
    name: String,
    description: String,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FormFieldLabel(
            text = stringResource(R.string.exercise_name_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = name,
            onTextChange = { onUserInputChange(ExerciseEditingEvent.NameChanged(it)) },
            label = stringResource(R.string.enter_exercise_name)
        )
        Spacer(Modifier.height(Dimens.large))
        FormFieldLabel(
            text = stringResource(R.string.exercise_description_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = description,
            onTextChange = { onUserInputChange(ExerciseEditingEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.enter_exercise_description),
            minLines = 3,
            maxLines = 5
        )
    }
}

@Composable
private fun CategorySelectionPanel(
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        FormFieldLabel(
            text = stringResource(R.string.target_muscles),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        CategoryFilters(
            categories = categories,
            toggleCategory = toggleCategory
        )
    }
}

@Preview
@Composable
fun CreateExerciseScreenPreview() {
    TrainingPlannerTheme {
        val bitmaps = remember { createPreviewBitmaps() }
        val state = ExerciseEditingState(
            images = bitmaps,
            previewImage = bitmaps.first(),
            categories = PREVIEW_CATEGORIES
        )
        CreateExerciseScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = ExerciseEditingUiState.Editing(state),
            onUserInputChange = {},
            onExerciseSaved = {}
        )
    }
}

@Preview
@Composable
fun CreateExerciseScreenSavingPreview() {
    TrainingPlannerTheme {
        val bitmaps = remember { createPreviewBitmaps() }
        val state = ExerciseEditingState(
            images = bitmaps,
            previewImage = bitmaps.first(),
            categories = PREVIEW_CATEGORIES
        )
        CreateExerciseScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = ExerciseEditingUiState.Saving(state),
            onUserInputChange = {},
            onExerciseSaved = {}
        )
    }
}