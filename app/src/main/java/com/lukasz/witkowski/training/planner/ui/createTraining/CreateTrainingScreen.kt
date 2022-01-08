package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import timber.log.Timber

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit,
    onAddExerciseClicked: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val exercises by viewModel.trainingExercises.collectAsState()
    var openDialog by remember { mutableStateOf(false) }
    var trainingExercise: TrainingExercise? = null

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.createTraining()
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Create training")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextDataInputs(title, viewModel, description)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onAddExerciseClicked() }) {
                Text(text = "Add Exercises")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TrainingExercisesList(
                exercises = exercises,
                removeTrainingExercise = { viewModel.removeTrainingExercise(it) },
                setRestTimeToTrainingExercise = {
                    trainingExercise = it
                    openDialog = true
                    Timber.d("Open dialog $openDialog, training Exercise $trainingExercise")
//                    viewModel.setRestTimeToExercise(it)
                }
            )
        }
        if(openDialog && trainingExercise != null) {
            SetTrainingExerciseRestTimeDialog(
                modifier = Modifier,
                trainingExercise = trainingExercise!!,
                setRestTimeToExercise = { trainingExercise, minutes, seconds ->
                    viewModel.setRestTimeToExercise(trainingExercise = trainingExercise, restTimeMinutes = minutes, restTimeSeconds = seconds)
                },
                closeDialog = { openDialog = false }
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
            Text(text = stringResource(id = R.string.rest_time_title), fontSize = 32.sp)
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
private fun TextDataInputs(
    title: String,
    viewModel: CreateTrainingViewModel,
    description: String
) {
    TextField(
        text = title,
        onTextChange = { viewModel.onTrainingTitleChanged(it) },
        label = "Title",
        imeAction = ImeAction.Next
    )
    Spacer(modifier = Modifier.height(16.dp))
    TextField(
        text = description,
        onTextChange = { viewModel.onTrainingDescriptionChanged(it) },
        label = "Description",
        imeAction = ImeAction.Next
    )
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
    Column(modifier = modifier.fillMaxWidth()) {
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
        Spacer(modifier = Modifier.height(8.dp))
    }
    
}

@Composable
private fun TrainingExerciseRestTime(
    modifier: Modifier = Modifier,
    setRestTimeToTrainingExercise: (TrainingExercise) -> Unit,
    trainingExercise: TrainingExercise
) {
    val restTime = trainingExercise.restTime
    val buttonText = if(restTime > 0L) "Change rest time" else "Add rest time"
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
        if(restTime > 0L) {
            Text(text = TimeFormatter.millisToTime(restTime), color = Color.Red, fontSize = 18.sp)
        }
    }
}

@Composable
private fun TrainingExerciseInfo(
    index: Int,
    trainingExercise: TrainingExercise,
    removeTrainingExercise: (TrainingExercise) -> Unit
) {
    ListCardItem() {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${index + 1}.", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = trainingExercise.exercise.name, fontSize = 24.sp)
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
                contentDescription = "Remove training exercise"
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
            text = "Reps: ${trainingExercise.repetitions}",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Sets: ${trainingExercise.sets}",
            modifier = Modifier.weight(1f)
        )
        if (trainingExercise.time > 0L) {
            Text(text = "Time: ${TimeFormatter.millisToMinutesSeconds(trainingExercise.time)}")
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
            exercise = Exercise(name = "New exercise"),
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
        trainingExercise = TrainingExercise(exercise = Exercise()),
        closeDialog = {},
        setRestTimeToExercise = { trainingExercise, i, i2 ->  }
    )
}