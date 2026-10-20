package com.lukasz.witkowski.training.planner.exercise.presentation.editor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppFilterChip
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppLoadingOverlay
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppTextField

@Composable
fun CreateExerciseScreen(
    viewModel: ExerciseEditorViewModel,
    navigateToDetails: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isEditMode = viewModel.isEditMode
    CreateExerciseScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = state,
        onUserInputChange = viewModel::onEvent,
        onExerciseSaved = navigateToDetails,
        isEditMode = isEditMode
    )
}

@Composable
fun CreateExerciseScreenContent(
    uiState: ExerciseEditingUiState,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    onExerciseSaved: (ExerciseId) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    when (uiState) {
        is ExerciseEditingUiState.Saving -> LoadingExerciseForm(
            exerciseEditingInput = uiState.exerciseEditingInput,
            isEditMode = isEditMode,
            modifier = modifier
        )

        is ExerciseEditingUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("loading"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is ExerciseEditingUiState.Editing -> ExerciseForm(
            state = uiState.exerciseEditingInput,
            modifier = modifier,
            onUserInputChange = onUserInputChange,
            isEditMode = isEditMode
        )

        is ExerciseEditingUiState.Failure -> {
            val failMessage = if (isEditMode) {
                stringResource(
                    R.string.failed_update_exercise,
                    uiState.message
                )
            } else {
                stringResource(R.string.failed_save_exercise, uiState.message)
            }
            LaunchedEffect(uiState) {
                Toast.makeText(
                    context,
                    failMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            uiState.exerciseEditingInput?.let {
                ExerciseForm(
                    state = it,
                    modifier = modifier,
                    onUserInputChange = onUserInputChange,
                    isEditMode = isEditMode
                )
            }
        }

        is ExerciseEditingUiState.Saved -> LaunchedEffect(uiState) {
            onExerciseSaved(uiState.exerciseId)
        }
    }
}

@Composable
private fun LoadingExerciseForm(
    exerciseEditingInput: ExerciseEditingInput,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    AppLoadingOverlay(
        isLoading = true,
        loadingTitle = stringResource(R.string.saving_exercise),
        modifier = Modifier.fillMaxSize()
    ) {
        ExerciseForm(
            state = exerciseEditingInput,
            modifier = modifier,
            isEditMode = isEditMode,
            onUserInputChange = {}
        )
    }
}

@Composable
private fun ExerciseForm(
    state: ExerciseEditingInput,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(Dimens2.spaceMd)
            .verticalScroll(rememberScrollState())
            .testTag("ExerciseForm"),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
    ) {
        ImageInputForm(
            images = state.images,
            previewImage = state.previewImage
        )
        TextInputForm(
            name = state.name,
            description = state.description,
            onUserInputChange = onUserInputChange
        )
        CategorySelectionPanel(
            categories = state.categories,
            toggleCategory = { onUserInputChange(ExerciseEditingEvent.CategoryToggled(it)) }
        )
        RecommendedParametersForm(
            recommendations = state.recommendations,
            onRecommendationChange = onUserInputChange
        )
        val stringRes = if (isEditMode) R.string.update_exercise else R.string.create_exercise
        AppPrimaryButton(
            text = stringResource(stringRes),
            onClick = { onUserInputChange(ExerciseEditingEvent.SaveChangesRequested(state)) },
            enabled = state.isValidForCreation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SaveButton")
        )
    }
}

@Composable
private fun TextInputForm(
    name: String,
    description: String,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.exercise_name_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens2.spaceXs)
        )
        AppTextField(
            value = name,
            onValueChange = { onUserInputChange(ExerciseEditingEvent.NameChanged(it)) },
            placeholder = stringResource(R.string.enter_exercise_name),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("NameField")
        )
        Spacer(Modifier.height(Dimens2.spaceMd))
        Text(
            text = stringResource(R.string.exercise_description_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens2.spaceXs)
        )
        AppTextField(
            value = description,
            onValueChange = { onUserInputChange(ExerciseEditingEvent.DescriptionChanged(it)) },
            placeholder = stringResource(R.string.enter_exercise_description),
            singleLine = false,
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("DescriptionField")
        )
    }
}

@Composable
private fun CategorySelectionPanel(
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Text(
            text = stringResource(R.string.target_muscles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            categories.forEach { filterCategory ->
                AppFilterChip(
                    label = filterCategory.category.name,
                    selected = filterCategory.isSelected,
                    onClick = { toggleCategory(filterCategory.category) },
                    modifier = Modifier.testTag(
                        "Chip${filterCategory.category.name}, isClickable=true"
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun CreateExerciseScreenPreview() {
    TrainingPlannerTheme2 {
        val bitmaps = remember { createPreviewBitmaps() }
        val state = ExerciseEditingInput(
            images = bitmaps,
            previewImage = bitmaps.first(),
            categories = PREVIEW_CATEGORIES
        )
        CreateExerciseScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = ExerciseEditingUiState.Editing(state),
            onUserInputChange = {},
            onExerciseSaved = {},
            isEditMode = true
        )
    }
}

@Preview
@Composable
fun CreateExerciseScreenSavingPreview() {
    TrainingPlannerTheme2 {
        val bitmaps = remember { createPreviewBitmaps() }
        val state = ExerciseEditingInput(
            images = bitmaps,
            previewImage = bitmaps.first(),
            categories = PREVIEW_CATEGORIES
        )
        CreateExerciseScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = ExerciseEditingUiState.Saving(state),
            onUserInputChange = {},
            onExerciseSaved = { },
            isEditMode = false
        )
    }
}
