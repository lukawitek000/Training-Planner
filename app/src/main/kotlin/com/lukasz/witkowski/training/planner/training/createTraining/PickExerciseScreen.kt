package com.lukasz.witkowski.training.planner.training.createTraining

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
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesScreenContent
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.components.TimerTimePicker

@Composable
fun PickExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    createTrainingViewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    var openTrainingExerciseConfigurationDialog by remember { mutableStateOf(false) }
    var openExerciseAlreadyAddedDialog by remember { mutableStateOf(false) }
    val pickedTrainingExercise by createTrainingViewModel.pickedExercise.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            AddSelectedExercisesToTrainingPlanFab(navigateBack = navigateBack)
        }
    ) {
        ExercisesScreenContent(
            viewModel = viewModel,
            isPickingExerciseMode = true,
            onExerciseClicked = { pickedExercise ->
                val isExerciseInTrainingPlan = createTrainingViewModel.pickExercise(pickedExercise)
                if (isExerciseInTrainingPlan) {
                    openExerciseAlreadyAddedDialog = true
                } else {
                    openTrainingExerciseConfigurationDialog = true
                }
            },
            pickedExercisesId = createTrainingViewModel.pickedExercisesIds
        )
        if (openExerciseAlreadyAddedDialog) {
            ExerciseAlreadyAddedDialog(
                exercise = pickedTrainingExercise!!,
                closeInfoDialog = { openExerciseAlreadyAddedDialog = false },
                openSettingExerciseDialog = { openTrainingExerciseConfigurationDialog = true }
            )
        }
        if (openTrainingExerciseConfigurationDialog) {
            SetTrainingExercisePropertiesDialog(
                exercise = pickedTrainingExercise!!,
                closeDialog = { openTrainingExerciseConfigurationDialog = false },
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
private fun AddSelectedExercisesToTrainingPlanFab(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            navigateBack()
        }) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = stringResource(id = R.string.add_exercises_to_training)
        )
    }
}

@Composable
private fun ExerciseAlreadyAddedDialog(
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
                    Text(text = stringResource(id = R.string.yes))
                }
                Button(onClick = {
                    closeInfoDialog()
                    openSettingExerciseDialog()
                }) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        }
    }
}


@Composable
fun SetTrainingExercisePropertiesDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    closeDialog: () -> Unit,
    saveTrainingExercise: (reps: String, sets: String, minutes: Int, seconds: Int) -> Unit
) {
    var reps by remember { mutableStateOf("1") }
    var sets by remember { mutableStateOf("1") }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }
    var isTimerSetEnable by remember { mutableStateOf(true) }

    DialogContainer(
        closeDialog = closeDialog,
        saveData = {
            saveTrainingExercise(reps, sets, minutes, seconds)
        }) {

        Column(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exercise.name,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colors.primary
            )
            SetsAndRepsInput(
                modifier = Modifier,
                reps = reps,
                setReps = { reps = it },
                sets = sets,
                setSets = { sets = it }
            )
            TimerTimePicker(
                modifier = Modifier.padding(top = 16.dp),
                minutes = minutes,
                seconds = seconds,
                onMinutesChange = { minutes = it },
                onSecondsChange = { seconds = it },
                isTimePickerEnabled = isTimerSetEnable,
            )
            TimerSetCheckbox(
                modifier = Modifier.align(Alignment.Start),
                isTimerSetEnable = isTimerSetEnable,
                toggleCheckbox = { isTimerSetEnable = !isTimerSetEnable }
            )
        }
    }
}

@Composable
private fun SetsAndRepsInput(
    modifier: Modifier = Modifier,
    reps: String,
    setReps: (String) -> Unit,
    sets: String,
    setSets: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            text = reps,
            onTextChange = setReps,
            label = stringResource(id = R.string.reps),
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = sets,
            onTextChange = setSets,
            label = stringResource(id = R.string.sets),
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
private fun TimerSetCheckbox(
    modifier: Modifier = Modifier,
    isTimerSetEnable: Boolean,
    toggleCheckbox: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { toggleCheckbox() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = !isTimerSetEnable,
            onCheckedChange = { toggleCheckbox() },
            colors = CheckboxDefaults.colors(uncheckedColor = MaterialTheme.colors.primary)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(id = R.string.do_not_set_timer),
            color = MaterialTheme.colors.primary
        )
    }
}
