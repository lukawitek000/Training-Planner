package com.lukasz.witkowski.training.planner.training.trainingSession

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.trainingSession.components.TimerWithCircularProgressBar
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TrainingExerciseRepsSetsTimeOverviewRow
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun TrainingExerciseScreen(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise,
    remainingTime: Time,
    start: () -> Unit,
    pause: () -> Unit,
    reset: () -> Unit,
    isTimerRunning: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurrentExerciseInformation(
            exercise = exercise
        )
        Spacer(Modifier.height(24.dp))
        CurrentExerciseTimer(
            totalTime = exercise.time,
            remainingTime = remainingTime,
            start = start,
            pause = pause,
            reset = reset,
            isTimerRunning = isTimerRunning
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
        TrainingExerciseRepsSetsTimeOverviewRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            exercise = exercise,
            fontSize = 20.sp,
            textColor = MaterialTheme.colors.primaryVariant
        )
    }
}

@Composable
private fun GeneralExerciseInformation(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        exercise.image?.let { image ->
            ImageContainer(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1.0f),
            ) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.exercise_image)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column {
            Text(
                text = exercise.name,
                fontSize = 32.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(text = exercise.description)
        }
    }
}

@Composable
fun CurrentExerciseTimer(
    modifier: Modifier = Modifier,
    totalTime: Time,
    remainingTime: Time,
    start: () -> Unit,
    pause: () -> Unit,
    reset: () -> Unit,
    isTimerRunning: Boolean
) {
    if (totalTime.isNotZero()) {
        ListCardItem(modifier = modifier) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimerWithCircularProgressBar(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .aspectRatio(1.0f),
                    totalTime = totalTime,
                    timeLeft = remainingTime
                )
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
                time = Time(30000),
                restTime = Time(60000)
            ),
            remainingTime = Time(1000L),
            start = {},
            pause = {},
            reset = {},
            isTimerRunning = false
        )
    }
}
