package com.lukasz.witkowski.training.planner.training.presentation.editor

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.lukasz.witkowski.training.planner.exercise.presentation.editor.buildStringOverview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPillStyle
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppLabeledTextField
import com.lukasz.witkowski.training.planner.shared.ui.timepicker.AppTimePickerBottomSheet
import com.lukasz.witkowski.training.planner.training.R
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TrainingPlanEditorScreen(
    viewModel: TrainingPlanEditorViewModel,
    onAddExerciseClicked: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val failureMessage = stringResource(R.string.failed_to_save_training_plan)
    LaunchedEffect(Unit) {
        viewModel.savingResult.collect { result ->
            when (result) {
                is SavingResult.Success -> navigateBack()
                is SavingResult.Failure -> {
                    Toast.makeText(
                        context,
                        failureMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    val state by viewModel.uiState.collectAsState()
    TrainingPlanEditorScreenContent(
        state = state,
        isEditMode = viewModel.isEditMode,
        onIntent = viewModel::processIntent,
        onAddExerciseClicked = onAddExerciseClicked,
        modifier = modifier
    )
}

@Composable
fun TrainingPlanEditorScreenContent(
    state: TrainingPlanConfiguration,
    onIntent: (TrainingPlanEditingIntent) -> Unit,
    onAddExerciseClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false
) {
    var showRestTimePicker by rememberSaveable { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = state.exercises.indexOfFirst { it.id.toString() == from.key }
        val toIndex = state.exercises.indexOfFirst { it.id.toString() == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            onIntent(TrainingPlanEditingIntent.TrainingExercisesReordered(fromIndex, toIndex))
        }
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(Dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        item {
            TextInputForm(
                title = state.title,
                description = state.description,
                onUserInputChange = onIntent
            )
        }
        item {
            CategoriesOverview(categories = state.categories)
        }
        if (state.exercises.isEmpty()) {
            item {
                TrainingExercisesListPlaceholder(
                    onAddExerciseClicked = onAddExerciseClicked
                )
            }
        } else {
            itemsIndexed(
                items = state.exercises,
                key = { _, it -> it.id.toString() }
            ) { index, exercise ->
                ReorderableItem(reorderableState, key = exercise.id.toString()) { isDragging ->
                    val elevation = if (isDragging) Dimens.shadowLg else Dimens.shadowSm
                    SwipeToDeleteExercise(
                        exercise = exercise,
                        index = index,
                        onDismiss = {
                            onIntent(TrainingPlanEditingIntent.TrainingExerciseRemoved(exercise))
                        },
                        dragIconModifier = Modifier.draggableHandle()
                    )
                }
            }
            item {
                AddExercisesButton(onAddExerciseClicked = onAddExerciseClicked)
            }
        }
        item {
            TrainingPlanRestTime(
                onShowRestTimePicker = { showRestTimePicker = true },
                restTime = state.restTime
            )
        }
        item {
            AppPrimaryButton(
                text = if (isEditMode) {
                    stringResource(R.string.save_training_plan_changes)
                } else {
                    stringResource(R.string.save_training_plan)
                },
                onClick = { onIntent(TrainingPlanEditingIntent.SaveTrainingPlan) },
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showRestTimePicker) {
        AppTimePickerBottomSheet(
            initialTime = Time(state.restTime.inWholeMilliseconds),
            onTimeConfirmed = { selectedTime ->
                if (selectedTime != null) {
                    onIntent(
                        TrainingPlanEditingIntent.RestTimeChanged(
                            selectedTime.timeInMillis.milliseconds
                        )
                    )
                }
            },
            onDismissRequest = { showRestTimePicker = false }
        )
    }
}

@Composable
private fun TextInputForm(
    title: String,
    description: String,
    onUserInputChange: (TrainingPlanEditingIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        AppLabeledTextField(
            label = stringResource(R.string.training_plan_title_label),
            value = title,
            onValueChange = { onUserInputChange(TrainingPlanEditingIntent.TitleChanged(it)) },
            placeholder = stringResource(R.string.training_plan_title),
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        AppLabeledTextField(
            label = stringResource(R.string.training_plan_description_label),
            value = description,
            onValueChange = { onUserInputChange(TrainingPlanEditingIntent.DescriptionChanged(it)) },
            placeholder = stringResource(R.string.training_plan_description),
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun CategoriesOverview(
    categories: List<ExerciseCategoryName>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
    ) {
        Text(
            text = stringResource(R.string.plan_categories),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.plan_categories_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.no_exercises_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Dimens.spaceXs)
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spaceXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
            ) {
                categories.forEach { category ->
                    AppCategoryPill(
                        text = category.name,
                        style = AppCategoryPillStyle.Tonal
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingExercisesListPlaceholder(
    onAddExerciseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            Text(
                text = stringResource(R.string.no_exercises_added_yet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.add_exercises_from_catalogue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppPrimaryButton(
                text = stringResource(R.string.add_exercises),
                onClick = onAddExerciseClicked,
                leadingIcon = Icons.Default.Add
            )
        }
    }
}

@Composable
private fun SwipeToDeleteExercise(
    exercise: TrainingExercise,
    index: Int,
    onDismiss: () -> Unit,
    dragIconModifier: Modifier,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = Dimens.spaceLg),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.remove_training_exercise),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = {
            AppCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.spaceXl)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
                    ) {
                        Text(
                            text = exercise.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val exerciseConfig = buildStringOverview(
                            sets = exercise.sets,
                            reps = exercise.repetitions,
                            restTime = exercise.restTime,
                            weightInKg = exercise.weightInKg
                        )
                        Text(
                            text = exerciseConfig,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragIconModifier
                    )
                }
            }
        }
    )
}

@Composable
private fun AddExercisesButton(onAddExerciseClicked: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onAddExerciseClicked,
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = Dimens.thinBorder,
            color = MaterialTheme.colorScheme.primaryContainer
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(vertical = Dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(Dimens.spaceSm))
            Text(
                text = stringResource(R.string.add_exercises),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun TrainingPlanRestTime(
    onShowRestTimePicker: () -> Unit,
    restTime: Duration,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedRestTime = TimeFormatter(context).formatTime(Time(restTime.inWholeMilliseconds))
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
    ) {
        Text(
            text = stringResource(R.string.rest_time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        AppCard(
            modifier = Modifier.clickable { onShowRestTimePicker() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    text = formattedRestTime,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.spaceMd),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun TrainingPlanEditorScreenContentPreview() {
    TrainingPlannerTheme {
        TrainingPlanEditorScreenContent(
            state = TrainingPlanConfiguration(
                title = "Push Workout",
                description = "Chest and triceps focused",
                exercises = emptyList(),
                restTime = 60.seconds
            ),
            modifier = Modifier.fillMaxSize(),
            onIntent = {},
            onAddExerciseClicked = {}
        )
    }
}
