package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.Exercise
import com.lukasz.witkowski.training.planner.exercise.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.training.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesScreenContent
import timber.log.Timber

@Composable
fun PickExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    createTrainingViewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    val pickedTrainingExercises by createTrainingViewModel.trainingExercises.collectAsState()
    var openDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val trainingTitle by createTrainingViewModel.title.collectAsState()
    val pickedTrainingExercise by createTrainingViewModel.pickedExercise.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Pick Exercises")
            }
        }
    ) {
        ExercisesScreenContent(
            viewModel = viewModel,
            pickingExerciseMode = true,
            pickExercise = { pickedExercise ->
                createTrainingViewModel.pickExercise(pickedExercise)
                if(pickedTrainingExercises.any { it.id == pickedExercise.id.value }) {
                    showInfoDialog = true
                } else {
                    openDialog = true
                }
            },
            pickedExercisesId = pickedTrainingExercises.map { it.id }
        )
        if(showInfoDialog) {
            InfoDialog(
                exercise = pickedTrainingExercise!!,
                closeInfoDialog = { showInfoDialog = false },
                openSettingExerciseDialog = { openDialog = true }
            )
        }
        if (openDialog) {
            SetTrainingExercisePropertiesDialog(
                exercise = pickedTrainingExercise!!,
                trainingTitle = trainingTitle,
                closeDialog = { openDialog = false },
                saveTrainingExercise = { reps, sets, minutes, seconds ->
                    createTrainingViewModel.createTrainingExercise(
                        pickedTrainingExercise!!,
                        reps,
                        sets,
                        minutes,
                        seconds
                    )
                }
            )
        }
    }

}

@Composable
private fun InfoDialog(
    exercise: Exercise,
    closeInfoDialog: () -> Unit,
    openSettingExerciseDialog: () -> Unit
) {
    DialogContainer(
        closeDialog = closeInfoDialog,
        saveData = {},
        showFab = false
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                text = stringResource(
                    id = R.string.add_training_exercise_one_more_time_info,
                    exercise.name
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = closeInfoDialog) {
                    Text(text = "No")
                }
                Button(onClick = {
                    closeInfoDialog()
                    openSettingExerciseDialog()
                }) {
                    Text(text = "Yes")
                }
            }
        }
    }
}


@Composable
fun SetTrainingExercisePropertiesDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    trainingTitle: String,
    closeDialog: () -> Unit,
    saveTrainingExercise: (reps: String, sets: String, minutes: Int, seconds: Int) -> Unit
) {
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }
    var isTimerSetEnable by remember { mutableStateOf(true) }

    DialogContainer(
        closeDialog = closeDialog,
        saveData = { saveTrainingExercise(reps, sets, minutes, seconds) }) {

        Column(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configure the ${exercise.name} exercise for the $trainingTitle training",
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colors.primary
            )

            TextField(
                text = reps,
                onTextChange = { reps = it },
                label = "Reps",
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = sets,
                onTextChange = { sets = it },
                label = "Sets",
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isTimerSetEnable) {
                TimerTimePicker(
                    minutes = minutes,
                    seconds = seconds,
                    onMinutesChange = {
                        Timber.d("New value minutes $it")
                        minutes = it
                    },
                    onSecondsChange = {
                        Timber.d("New value seconds $it")
                        seconds = it
                    },
                    isTimePickerEnabled = isTimerSetEnable
                )
            } else {
                TimerTimePicker(
                    minutes = 0,
                    seconds = 0,
                    onMinutesChange = {},
                    onSecondsChange = {},
                    isTimePickerEnabled = isTimerSetEnable
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        isTimerSetEnable = !isTimerSetEnable
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = !isTimerSetEnable,
                    onCheckedChange = { isTimerSetEnable = !isTimerSetEnable },
                    colors = CheckboxDefaults.colors(uncheckedColor = MaterialTheme.colors.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Do not set timer",
                    color = MaterialTheme.colors.primary
                )
            }
        }

    }
}


