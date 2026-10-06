package com.lukasz.witkowski.training.planner.training.editor

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import com.lukasz.witkowski.training.planner.ui.components.buildStringOverview
import com.lukasz.witkowski.training.planner.ui.theme.DarkGrey
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightGrey
import com.lukasz.witkowski.training.planner.ui.theme.MediumGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrainingPlanEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingPlanEditorViewModel,
    navigateBack: () -> Unit,
    onAddExerciseClicked: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    TrainingPlanEditorScreenContent(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier
    )
}

@Composable
private fun TrainingPlanEditorScreenContent(
    state: TrainingPlanConfiguration,
    onIntent: (TrainingPlanEditingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        TextInputForm(
            title = state.title,
            description = state.description,
            onUserInputChange = onIntent
        )
        CategoriesOverview(
            categories = state.categories
        )
        if (state.exercises.isEmpty()) {
            TrainingExercisesListPlaceholder(
                onAddExerciseClicked = {},
            )
        } else {
            TrainingExercisesList(
                exercises = state.exercises,
                modifier = Modifier.fillMaxWidth(),
                onAddExerciseClicked = {}
            )
        }
        ConfirmButton(
            text = stringResource(R.string.save_training_plan),
            onClick = {}
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
            label = stringResource(R.string.enter_training_plan_title),
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

@Composable
private fun TrainingExercisesList(
    exercises: List<TrainingExercise>,
    onAddExerciseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        item {
            Text(stringResource(R.string.exercises_count, exercises.size))
        }
        itemsIndexed(exercises) { index, exercise ->
            TrainingExerciseItem(
                exercise = exercise,
                index = index
            )
        }
        item {
            AddExercisesButton(
                onAddExerciseClicked = onAddExerciseClicked
            )
        }
    }
}

@Composable
private fun TrainingExerciseItem(
    exercise: TrainingExercise,
    index: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.normal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.large)
        ) {
            TrainingExercisePosition(position = index + 1)
            TrainingExerciseOverview(
                exercise = exercise,
                modifier = Modifier.weight(1f)
            )
            Icon(imageVector = Icons.Default.DragHandle, contentDescription = null)
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
private fun ConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ) {
        Text(text)
    }

}

private fun LazyListScope.trainingExercisesList(
    trainingExercises: List<TrainingExercise>,
    removeTrainingExercise: (TrainingExercise) -> Unit,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit
) {
    itemsIndexed(trainingExercises) { index, exercise ->
        TrainingExerciseListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            trainingExercise = exercise,
            index = index,
            removeTrainingExercise = removeTrainingExercise,
            setRestTimeToTrainingExercise = setRestTimeToTrainingExercise
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
fun TrainingExerciseListItem(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise,
    index: Int = 0,
    removeTrainingExercise: (TrainingExercise) -> Unit,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, LightDark12, RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        TrainingExerciseInfo(
            index = index,
            trainingExercise = trainingExercise,
            removeTrainingExercise = removeTrainingExercise
        )
        Spacer(modifier = Modifier.height(4.dp))
        TrainingExerciseRestTime(
            setRestTimeToTrainingExercise = setRestTimeToTrainingExercise,
            trainingExercise = trainingExercise
        )
    }
}

@Composable
private fun TrainingExerciseRestTime(
    modifier: Modifier = Modifier,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit,
    trainingExercise: TrainingExercise
) {
    val restTime = trainingExercise.restTime
    val buttonText = if (restTime.isPositive()) {
        stringResource(id = R.string.change_rest_time)
    } else {
        stringResource(id = R.string.add_rest_time)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            setRestTimeToTrainingExercise(trainingExercise)
        }) {
            Text(text = buttonText)
        }
        if (restTime.isPositive()) {
            Text(
                text = "TimeFormatter(LocalContext.current).formatTime(restTime)",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun SetTrainingExerciseRestTimeDialog(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise,
    setRestTimeToExercise: (TrainingExercise, Int, Int) -> Unit,
    closeDialog: () -> Unit
) {
//    val (currentMinutes, currentSeconds) = trainingExercise.restTime.minutesAndSeconds()
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    DialogContainer(
        closeDialog = closeDialog,
        saveData = { setRestTimeToExercise(trainingExercise, minutes, seconds) }) {
        Column(
            modifier = modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.rest_time),
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.rest_time_info), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            TimerTimePicker(
                modifier = Modifier,
                minutes = minutes,
                seconds = seconds,
                onMinutesChange = { minutes = it },
                onSecondsChange = { seconds = it }
            )
        }
    }
}

@Composable
private fun TrainingExerciseInfo(
    modifier: Modifier = Modifier,
    index: Int,
    trainingExercise: TrainingExercise,
    removeTrainingExercise: (TrainingExercise) -> Unit
) {
    ListCardItem(modifier = modifier) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}.",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = trainingExercise.exercise.name,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseSetsRepsTimeInfo(trainingExercise = trainingExercise)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        removeTrainingExercise(trainingExercise)
                    },
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.remove_training_exercise),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExerciseSetsRepsTimeInfo(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.reps_with_value, trainingExercise.repetitions),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = stringResource(id = R.string.sets_with_value, trainingExercise.sets),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondary
        )
//        if (trainingExercise.time.isNotZero()) {
//            Text(
//                text = stringResource(
//                    id = R.string.time_with_value,
//                    TimeFormatter(LocalContext.current).formatTime(trainingExercise.time)
//                ),
//                color = MaterialTheme.colorScheme.secondary
//            )
//        } else {
//            Spacer(modifier = Modifier.weight(1f))
//        }
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
                onIntent = {}
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
                onIntent = {}
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
    restTime = 60.seconds
)

