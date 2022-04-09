package com.lukasz.witkowski.training.planner.trainingSession

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.createTraining.ExerciseSetsRepsTimeInfo
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.trainingSession.components.FabTextWithIcon
import com.lukasz.witkowski.training.planner.trainingSession.components.TimerWithCircularProgressBar
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun TrainingExerciseScreen(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise,
    remainingTime: Long,
    start: () -> Unit,
    pause: () -> Unit,
    reset: () -> Unit,
    isTimerRunning: Boolean,
    skip: () -> Unit,
    completed: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CurrentExerciseInformation(
                exercise = exercise
            )
            Spacer(Modifier.height(32.dp))
            CurrentExerciseTimer(
                totalTime = exercise.time,
                remainingTime = remainingTime,
                start = start,
                pause = pause,
                reset = reset,
                isTimerRunning = isTimerRunning
            )
        }
        CompletedAndSkipFabs(
            modifier = Modifier.padding(top = 8.dp),
            completed = completed,
            skip = skip
        )
    }
}

@Composable
fun CurrentExerciseInformation(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Column(modifier = modifier) {
        GeneralExerciseInformation(exercise = exercise)
        Spacer(Modifier.height(8.dp))
        ExerciseSetsRepsTimeInfo(
            modifier = Modifier.padding(horizontal = 16.dp),
            trainingExercise = exercise
        )
    }
}

@Composable
private fun GeneralExerciseInformation(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Row(modifier = modifier) {
        exercise.image?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = stringResource(id = R.string.exercise_image)
            )
        }
        Column {
            Text(text = exercise.name, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = exercise.description)
        }
    }
}

@Composable
fun CurrentExerciseTimer(
    modifier: Modifier = Modifier,
    totalTime: Long,
    remainingTime: Long,
    start: () -> Unit,
    pause: () -> Unit,
    reset: () -> Unit,
    isTimerRunning: Boolean
) {
    ListCardItem() {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerWithCircularProgressBar(totalTime = totalTime, timeLeft = remainingTime)
            Spacer(modifier = Modifier.height(16.dp))
            TimerControlButtons(
                start = start,
                pause = pause,
                reset = reset,
                isTimerRunning = isTimerRunning
            )
        }
    }
}

@Composable
fun TimerControlButtons(
    modifier: Modifier = Modifier,
    start: () -> Unit,
    pause: () -> Unit,
    reset: () -> Unit,
    isTimerRunning: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = if (isTimerRunning) pause else start) {
            Icon(
                imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(id = if (isTimerRunning) R.string.pause else R.string.start)
            )
        }
        Button(onClick = reset) {
            Icon(
                imageVector = Icons.Filled.Replay,
                contentDescription = stringResource(id = R.string.reset)
            )
        }
    }
}

@Composable
fun CompletedAndSkipFabs(
    modifier: Modifier = Modifier,
    completed: () -> Unit,
    skip: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FabTextWithIcon(
            text = stringResource(id = R.string.skip),
            imageVector = Icons.Filled.SkipNext,
            onClick = skip
        )
        FabTextWithIcon(
            text = stringResource(id = R.string.completed),
            imageVector = Icons.Filled.Check,
            onClick = completed
        )
    }
}

@Preview
@Composable
private fun TrainingExerciseScreenPreview() {
    TrainingPlannerTheme {
        TrainingExerciseScreen(
            exercise = TrainingExercise(
                id = TrainingExerciseId(""),
                name = "Test exercise",
                description = "Test exercise description",
                category = Category(0, R.string.category_back),
                repetitions = 15,
                sets = 3,
                time = 30000,
                restTime = 60000
            ),
            remainingTime = 1000L,
            completed = {},
            skip = {},
            start = {},
            pause = {},
            reset = {},
            isTimerRunning = false
        )
    }
}
