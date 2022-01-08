package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesScreenContent
import timber.log.Timber

@Composable
fun PickExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    createTrainingViewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    //val pickedExercises by viewModel.pickedExercises.collectAsState()
    val pickedTrainingExercises by createTrainingViewModel.trainingExercises.collectAsState()
    var openDialog by remember { mutableStateOf(false) }
    val trainingTitle by createTrainingViewModel.title.collectAsState()

    var exercise = Exercise()
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                //createTrainingViewModel.addPickedTrainingExercises()
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Pick Exercises")
            }
        }
    ) {
        ExercisesScreenContent(
            viewModel = viewModel,
            pickingExerciseMode = true,
            pickExercise = {
                exercise = it
                openDialog = true
            },
            pickedExercises = pickedTrainingExercises.map { it.exercise }
        )
        if (openDialog) {
            SetTrainingExercisePropertiesDialog(
                exercise = exercise,
                trainingTitle = trainingTitle,
                closeDialog = { openDialog = false },
                saveTrainingExercise = { reps, sets, minutes, seconds ->
                    createTrainingViewModel.createTrainingExercise(
                        exercise,
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

    Dialog(
        onDismissRequest = { closeDialog() })
    {
        Scaffold(
            modifier = Modifier
                .height(600.dp)
                .clip(MaterialTheme.shapes.medium),
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    saveTrainingExercise(reps, sets, minutes, seconds)
                    closeDialog()
                }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Done exercise configuration"
                    )
                }
            },
            backgroundColor = Color.Gray
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure the ${exercise.name} exercise for the $trainingTitle training",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
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

                Spacer(modifier = Modifier.weight(1f))

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
                        onCheckedChange = { isTimerSetEnable = !isTimerSetEnable }
                    )
                    Text(
                        text = "Do not set timer"
                    )
                }
            }
        }
    }
}
