package com.lukasz.witkowski.training.planner.training.createTraining

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.models.TrainingExercise
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit,
    onAddExerciseClicked: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val trainingExercises by viewModel.trainingExercises.collectAsState()
    var pickedTrainingExercise by remember { mutableStateOf<TrainingExercise?>(null) }
    val canTrainingPlanBeCreated = title.isNotEmpty() && trainingExercises.isNotEmpty()
    var showNoEnoughDataToCreateTrainingPlanToast by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            CreateTrainingPlanFab(
                modifier = Modifier,
                fabClicked = {
                    viewModel.createTrainingPlan()
                    navigateBack()
                },
                canTrainingPlanBeCreated = canTrainingPlanBeCreated,
                showToast = { showNoEnoughDataToCreateTrainingPlanToast = true }
            )
        }
    ) {
        NoEnoughDataToast(
            showNoEnoughDataToCreateTrainingPlanToast = showNoEnoughDataToCreateTrainingPlanToast,
            hideToast = { showNoEnoughDataToCreateTrainingPlanToast = false }
        )
        CreateTrainingScreenContent(
            modifier = Modifier,
            title = title,
            onTitleChanged = { viewModel.onTrainingTitleChanged(it) },
            description = description,
            onDescriptionChanged = { viewModel.onTrainingDescriptionChanged(it) },
            onAddExerciseClicked = onAddExerciseClicked,
            trainingExercises = trainingExercises,
            setRestTimeToTrainingExercise = {
                pickedTrainingExercise = it
            },
            removeTrainingExercise = { viewModel.removeTrainingExercise(it) }
        )
        if (pickedTrainingExercise != null) {
            SetTrainingExerciseRestTimeDialog(
                modifier = Modifier,
                trainingExercise = pickedTrainingExercise!!,
                setRestTimeToExercise = { trainingExercise, minutes, seconds ->
                    viewModel.setRestTimeToExercise(
                        exercise = trainingExercise,
                        restTimeMinutes = minutes,
                        restTimeSeconds = seconds
                    )
                },
                closeDialog = { pickedTrainingExercise = null }
            )
        }
    }
}

@Composable
private fun CreateTrainingPlanFab(
    modifier: Modifier = Modifier,
    fabClicked: () -> Unit,
    canTrainingPlanBeCreated: Boolean,
    showToast: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            if (canTrainingPlanBeCreated) {
                fabClicked()
            } else {
                showToast()
            }
        },
    ) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = stringResource(id = R.string.create_training)
        )
    }
}

@Composable
private fun NoEnoughDataToast(
    showNoEnoughDataToCreateTrainingPlanToast: Boolean,
    hideToast: () -> Unit
) {
    if (showNoEnoughDataToCreateTrainingPlanToast) {
        Toast.makeText(
            LocalContext.current,
            stringResource(id = R.string.training_plan_no_enough_data_message),
            Toast.LENGTH_SHORT
        ).show()
        hideToast()
    }
}

@Composable
private fun CreateTrainingScreenContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChanged: (String) -> Unit,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onAddExerciseClicked: () -> Unit,
    trainingExercises: List<TrainingExercise>,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit,
    removeTrainingExercise: (TrainingExercise) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrainingAndDescriptionInputs(
            modifier = Modifier.padding(bottom = 16.dp),
            title = title,
            description = description,
            onTitleChanged = onTitleChanged,
            onDescriptionChanged = onDescriptionChanged
        )
        AddExercisesButton(
            modifier = Modifier.padding(bottom = 16.dp),
            onAddExerciseClicked = onAddExerciseClicked
        )
        TrainingExercisesList(
            exercises = trainingExercises,
            removeTrainingExercise = removeTrainingExercise,
            setRestTimeToTrainingExercise = setRestTimeToTrainingExercise
        )
    }
}

@Composable
private fun TrainingAndDescriptionInputs(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            text = title,
            onTextChange = { onTitleChanged(it) },
            label = stringResource(id = R.string.title),
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = description,
            onTextChange = { onDescriptionChanged(it) },
            label = stringResource(id = R.string.description),
            imeAction = ImeAction.Done,
            maxLines = 4
        )
    }
}

@Composable
private fun AddExercisesButton(
    modifier: Modifier = Modifier,
    onAddExerciseClicked: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onAddExerciseClicked() }
    ) {
        Text(text = stringResource(id = R.string.add_exercises))
    }
}

@Composable
fun TrainingExercisesList(
    modifier: Modifier = Modifier,
    exercises: List<TrainingExercise>,
    removeTrainingExercise: (TrainingExercise) -> Unit,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(exercises) { index, exercise ->
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
    val buttonText = if (restTime > 0L) {
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
        if (restTime > 0L) {
            Text(
                text = TimeFormatter.millisToTime(restTime),
                color = MaterialTheme.colors.primary,
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
    val (currentMinutes, currentSeconds) = TimeFormatter.calculateMinutesAndSeconds(trainingExercise.restTime)
    var minutes by remember { mutableStateOf(currentMinutes) }
    var seconds by remember { mutableStateOf(currentSeconds) }

    DialogContainer(
        closeDialog = closeDialog,
        saveData = { setRestTimeToExercise(trainingExercise, minutes, seconds) }) {
        Column(
            modifier = modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.rest_time_title),
                fontSize = 32.sp,
                color = MaterialTheme.colors.primary
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
            Text(text = "${index + 1}.", fontSize = 32.sp, color = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = trainingExercise.name,
                    fontSize = 24.sp,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseSetsRepsTimeInfo(trainingExercise)
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
                tint = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
private fun ExerciseSetsRepsTimeInfo(trainingExercise: TrainingExercise) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.reps, trainingExercise.repetitions),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colors.primaryVariant
        )
        Text(
            text = stringResource(id = R.string.sets, trainingExercise.sets),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colors.primaryVariant
        )
        if (trainingExercise.time > 0L) {
            Text(
                text = stringResource(
                    id = R.string.time,
                    TimeFormatter.millisToMinutesSeconds(trainingExercise.time)
                ),
                color = MaterialTheme.colors.primaryVariant
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
fun TrainingExerciseListItemPreview() {
    TrainingExerciseListItem(
        trainingExercise = TrainingExercise(
            id = TrainingExerciseId(""),
            name = "New exercise",
            description = "",
            repetitions = 10,
            sets = 5,
            time = 1000,
            restTime = 3000
        ),
        removeTrainingExercise = {},
        setRestTimeToTrainingExercise = {}
    )
}

@Preview
@Composable
fun RestTimeDialogPreview() {
    SetTrainingExerciseRestTimeDialog(
        trainingExercise = TrainingExercise(
            category = Category(),
            name = "Preview exercise",
            id = TrainingExerciseId("")
        ),
        closeDialog = {},
        setRestTimeToExercise = { trainingExercise, i, i2 -> }
    )
}