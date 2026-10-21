package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.components.OverlayLoading
import com.lukasz.witkowski.training.planner.ui.components.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CreateExerciseScreen(
    viewModel: ExerciseEditorViewModel,
    navigateToDetails: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when (uiState) {
        is ExerciseEditingUiState.Saving -> LoadingExerciseForm(
            uiState.exerciseEditingInput,
            isEditMode = isEditMode,
            modifier = modifier,
        )

        is ExerciseEditingUiState.Loading -> LoadingScreen(modifier)
        is ExerciseEditingUiState.Editing -> ExerciseForm(
            state = uiState.exerciseEditingInput,
            modifier = modifier,
            onUserInputChange = onUserInputChange,
            isEditMode = isEditMode
        )

        is ExerciseEditingUiState.Failure -> {
            val failMessage = if (isEditMode) stringResource(
                R.string.failed_update_exercise,
                uiState.message
            ) else stringResource(R.string.failed_save_exercise, uiState.message)
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
    modifier: Modifier = Modifier,
) {
    OverlayLoading(Modifier.fillMaxSize()) {
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                val bitmaps = uris.mapNotNull { uri ->
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }?.let { ImageBitmap(it) }
                    }.getOrNull()
                }
                if (bitmaps.isNotEmpty()) {
                    onUserInputChange(ExerciseEditingEvent.ImagesAdded(bitmaps))
                }
            }
        }
    )

    Column(
        modifier = modifier
            .padding(Dimens.normal)
            .verticalScroll(rememberScrollState())
            .testTag("ExerciseForm"),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        ImageInputForm(
            images = state.images,
            previewImage = state.previewImage,
            onAddImagesClicked = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveImageClicked = {
                onUserInputChange(ExerciseEditingEvent.ImageRemoved(it))
            },
            onSelectPreview = {
                onUserInputChange(ExerciseEditingEvent.ImagePreviewChanged(it))
            }
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
            onClick = { onUserInputChange(ExerciseEditingEvent.SaveChangesRequested(state)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SaveButton"),
            enabled = state.isValidForCreation()
        ) {
            val stringRes = if (isEditMode) R.string.update_exercise else R.string.create_exercise
            Text(stringResource(stringRes))
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
            label = stringResource(R.string.enter_exercise_name),
            modifier = Modifier.testTag("NameField")
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
            maxLines = 5,
            modifier = Modifier.testTag("DescriptionField")
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

private fun createPreviewBitmaps(): List<ImageBitmap> {
    val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
    return listOf(ImageBitmap(bitmap), ImageBitmap(bitmap))
}

@Preview
@Composable
fun CreateExerciseScreenPreview() {
    TrainingPlannerTheme {
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
    TrainingPlannerTheme {
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