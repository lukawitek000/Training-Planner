package com.lukasz.witkowski.training.planner.training.editor

import android.widget.Toast
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.ui.components.CategoriesRow
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.ConfirmButton
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.RestTimeBottomSheet
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import com.lukasz.witkowski.training.planner.ui.components.buildStringOverview
import com.lukasz.witkowski.training.planner.ui.theme.DarkGrey
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightGrey
import com.lukasz.witkowski.training.planner.ui.theme.MediumGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun TrainingPlanEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingPlanEditorViewModel,
    onAddExerciseClicked: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    TrainingPlanEditorScreenContent(
        state = state,
        onIntent = viewModel::processIntent,
        onAddExerciseClicked = onAddExerciseClicked,
        modifier = modifier,
    )
}

@Composable
private fun TrainingPlanEditorScreenContent(
    state: TrainingPlanConfiguration,
    onIntent: (TrainingPlanEditingIntent) -> Unit,
    onAddExerciseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRestTimePicker by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.padding(Dimens.normal),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        item {
            TextInputForm(
                title = state.title,
                description = state.description,
                onUserInputChange = onIntent
            )
        }
        item {
            CategoriesOverview(
                categories = state.categories
            )
        }
        if (state.exercises.isEmpty()) {
            item {
                TrainingExercisesListPlaceholder(
                    onAddExerciseClicked = onAddExerciseClicked,
                )
            }
        } else {
            trainingExercisesItems(
                exercises = state.exercises,
                onAddExerciseClicked = onAddExerciseClicked,
                onIntent = onIntent,
            )
        }
        item {
            TrainingPlanRestTime(
                onShowRestTimePicker = { showRestTimePicker = true },
                restTime = state.restTime
            )
        }

        item {
            ConfirmButton(
                text = stringResource(R.string.save_training_plan),
                onClick = {},
                isEnabled = state.isValid
            )
        }
    }
    if (showRestTimePicker) {
        RestTimeBottomSheet(
            onDismissRequest = { showRestTimePicker = false },
            defaultRestTime = state.restTime,
            onRestTimeConfigured = {
                onIntent(TrainingPlanEditingIntent.RestTimeChanged(it))
                showRestTimePicker = false
            }
        )
    }
}

@Composable
private fun TextInputForm(
    title: String,
    description: String,
    onUserInputChange: (TrainingPlanEditingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FormFieldLabel(
            text = stringResource(R.string.training_plan_title_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = title,
            onTextChange = { onUserInputChange(TrainingPlanEditingIntent.TitleChanged(it)) },
            label = stringResource(R.string.training_plan_title),
            modifier = Modifier.testTag("NameField")
        )
        Spacer(Modifier.height(Dimens.large))
        FormFieldLabel(
            text = stringResource(R.string.training_plan_description_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = description,
            onTextChange = { onUserInputChange(TrainingPlanEditingIntent.DescriptionChanged(it)) },
            label = stringResource(R.string.training_plan_description),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.testTag("DescriptionField")
        )
    }
}

@Composable
private fun CategoriesOverview(
    categories: List<ExerciseCategoryName>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        FormFieldLabel(
            text = stringResource(R.string.plan_categories),
            modifier = Modifier
        )
        Text(
            text = stringResource(R.string.plan_categories_caption),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.no_exercises_yet),
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.normal))
                    .background(Brush.horizontalGradient(colors = listOf(MediumGrey, DarkGrey)))
                    .padding(vertical = Dimens.small, horizontal = Dimens.normal)
            )
        } else {
            CategoriesRow(
                modifier = Modifier.fillMaxWidth(),
                categories = categories.map { it.name }
            )
        }
    }

}

@Composable
private fun TrainingExercisesListPlaceholder(
    onAddExerciseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.normal)
            .border(
                width = Dimens.border,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(Dimens.large)
            )
            .padding(Dimens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Icon(
            painter = painterResource(R.drawable.exercises_icon),
            contentDescription = null,
            modifier = Modifier
                .size(Dimens.imagePlaceholderSize)
                .padding(bottom = Dimens.normal),
        )
        Text(
            stringResource(R.string.no_exercises_added_yet),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.add_exercises_from_catalogue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )
        AddExercisesButton(
            onAddExerciseClicked = onAddExerciseClicked
        )
    }
}

private fun LazyListScope.trainingExercisesItems(
    exercises: List<TrainingExercise>,
    onAddExerciseClicked: () -> Unit,
    onIntent: (TrainingPlanEditingIntent) -> Unit,
) {
    item {
        Text(stringResource(R.string.exercises_count, exercises.size))
    }
    itemsIndexed(exercises, key = { _, item -> item.id.toString() }) { index, exercise ->
        TrainingExerciseItemWrapper(
            exercise = exercise,
            index = index,
            onDismiss = { onIntent(TrainingPlanEditingIntent.TrainingExerciseRemoved(exercise)) }
        )
    }
    item {
        AddExercisesButton(
            onAddExerciseClicked = onAddExerciseClicked
        )
    }
}

@Composable
private fun LazyItemScope.TrainingExerciseItemWrapper(
    exercise: TrainingExercise,
    index: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggedExercise by remember { mutableStateOf<TrainingExercise?>(null) }
    val state = rememberSwipeToDismissBoxState()
    val shape = RoundedCornerShape(Dimens.large)
    SwipeToDismissBox(
        state = state,
        modifier = modifier
            .animateItem(
                fadeInSpec = tween(300),
                fadeOutSpec = tween(300),
                placementSpec = spring()
            ),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red, shape = shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            }
        },
        content = {
            TrainingExerciseItem(
                exercise = exercise,
                index = index,
                shape = shape,
                dragIconModifier = Modifier.pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            Timber.i("Drag start ${exercise.exercise.name}")
                            draggedExercise = exercise
                        },
                        onDrag = { change, offset ->
                            Timber.i("onDrag ${offset}")
                            change.consume()
                        },
                        onDragEnd = {
                            Timber.i("onDragEnd")
                            draggedExercise = null
                        }
                    )
                }
            )
        },
        onDismiss = { onDismiss() }
    )
}

@Composable
private fun TrainingExerciseItem(
    exercise: TrainingExercise,
    index: Int,
    shape: Shape,
    dragIconModifier: Modifier,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = shape) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.normal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.large)
        ) {
            TrainingExercisePosition(position = index + 1)
            TrainingExerciseOverview(
                exercise = exercise,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                modifier = dragIconModifier
            )
        }
    }
}

@Composable
private fun TrainingExercisePosition(
    position: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(
                width = Dimens.border,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
            .sizeIn(minWidth = Dimens.xlarge, minHeight = Dimens.xlarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            modifier = Modifier.padding(Dimens.small),
            style = MaterialTheme.typography.bodyMedium.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}

@Composable
private fun TrainingExerciseOverview(
    exercise: TrainingExercise,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Text(
            text = exercise.exercise.name,
            style = MaterialTheme.typography.titleMedium
        )
        val exerciseConfig = buildStringOverview(
            sets = exercise.sets,
            reps = exercise.repetitions,
            restTime = exercise.restTime,
            weightInKg = exercise.weightInKg
        )
        Text(
            text = exerciseConfig,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

@Composable
private fun AddExercisesButton(
    modifier: Modifier = Modifier,
    onAddExerciseClicked: () -> Unit
) {
    Button(
        onClick = onAddExerciseClicked,
        modifier = modifier.padding(bottom = Dimens.normal),
        border = BorderStroke(width = Dimens.small, color = MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.normal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(Dimens.normal))
            Text(stringResource(R.string.add_exercises))
        }
    }
}

@Composable
private fun TrainingPlanRestTime(
    onShowRestTimePicker: () -> Unit,
    restTime: Duration,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        FormFieldLabel(
            text = stringResource(R.string.rest_time)
        )
        Button(
            onClick = onShowRestTimePicker,
            border = BorderStroke(width = Dimens.xsmall, color = MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(Dimens.small)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.normal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = stringResource(R.string.rest_time_picker_icon)
                )
                Text(
                    text = restTime.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.large),
                    style = MaterialTheme.typography.titleLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = stringResource(R.string.rest_time_picker_icon)
                )
            }
        }
    }
}

@Preview
@Composable
private fun TrainingPlanEditorScreenContentPreview() {
    TrainingPlannerTheme {
        Scaffold() { paddingValues ->
            TrainingPlanEditorScreenContent(
                state = PREVIEW_TRAINING_PLAN_CONFIGURATION,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onIntent = {},
                onAddExerciseClicked = {}
            )
        }
    }
}

@Preview
@Composable
private fun TrainingPlanEditorScreenContentEmptyExercisesPreview() {
    TrainingPlannerTheme {
        Scaffold() { paddingValues ->
            TrainingPlanEditorScreenContent(
                state = PREVIEW_TRAINING_PLAN_CONFIGURATION.copy(exercises = emptyList()),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onIntent = {},
                onAddExerciseClicked = {}
            )
        }
    }
}

private val PREVIEW_TRAINING_PLAN_CONFIGURATION = TrainingPlanConfiguration(
    title = "Full Body Routine",
    description = "Comprehensive full body workout for muscle building and strength.",
    exercises = listOf(
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Barbell Squat",
                description = "Compound leg exercise",
                categories = setOf(ExerciseCategoryName("Legs"))
            ),
            repetitions = 10,
            sets = 4,
            restTime = 90.seconds
        ),
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Bench Press",
                description = "Chest press exercise",
                categories = setOf(ExerciseCategoryName("Chest"), ExerciseCategoryName("Arms"))
            ),
            repetitions = 8,
            sets = 3,
            restTime = 60.seconds,
            weightInKg = 100
        ),
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Pull Ups",
                description = "Upper body bodyweight exercise",
                categories = setOf(ExerciseCategoryName("Back"), ExerciseCategoryName("Arms"))
            ),
            repetitions = 12,
            sets = 3,
            restTime = 60.seconds
        )
    ),
    restTime = 90.seconds
)

