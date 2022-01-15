package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.exercisesList.ImageWithDefaultPlaceholder

@Composable
fun CurrentTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CurrentTrainingViewModel,
    navigateBack: (String) -> Unit
) {
    val trainingFetchState by viewModel.trainingFetchState.collectAsState(initial = ResultHandler.Loading)
    val currentTrainingState by viewModel.currentTrainingState.observeAsState(initial = CurrentTrainingState.SummaryState)
    val timeLeft by viewModel.timerHelper.timeLeft.observeAsState(initial = 0L)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.navigateToTheNextScreen() }) {
                Text(text = "Next")
            }
        }
    ) {
        when(trainingFetchState) {
            is ResultHandler.Loading -> {
                LoadingTrainingContent()
            }
            is ResultHandler.Success -> {
                CurrentTrainingContent(
                    modifier = Modifier,
                    currentTrainingState = currentTrainingState,
                    timeLeft = timeLeft,
                    handleTimerAction = { viewModel.handleTimerAction(it) },
                    timerButtonText = "START/PAUSE/RESUME" // TODO text is not changing
                )
            }
            is ResultHandler.Error -> {
                navigateBack(stringResource(id = R.string.training_not_found))
            }
            is ResultHandler.Idle -> Unit
        }

    }

}

@Composable
private fun LoadingTrainingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun CurrentTrainingContent(
    modifier: Modifier,
    currentTrainingState: CurrentTrainingState,
    timeLeft: Long,
    handleTimerAction: (Long) -> Unit,
    timerButtonText: String
) {
    Column(modifier.fillMaxSize()) {
        when(currentTrainingState) {
            is CurrentTrainingState.ExerciseState -> {
                CurrentExerciseContent(
                    modifier = Modifier,
                    trainingExercise = currentTrainingState.exercise,
                    timeLeft = timeLeft,
                    handleTimerAction = handleTimerAction,
                    timerButtonText = timerButtonText
                )
            }
            is CurrentTrainingState.RestTimeState -> {
                CurrentRestTimeContent(
                    modifier = Modifier,
                    restTime = currentTrainingState.restTime,
                    timeLeft = timeLeft
                )
            }
            is CurrentTrainingState.SummaryState -> {
                CurrentTrainingSummaryContent(modifier = Modifier)
            }
        }
    }

}

@Composable
fun CurrentTrainingSummaryContent(modifier: Modifier) {
    Text(text = "Training summary")
}

@Composable
fun CurrentRestTimeContent(
    modifier: Modifier = Modifier,
    restTime: Long,
    timeLeft: Long
) {
    TimerWithProgressBar(
        modifier
            .fillMaxSize(),
        timeLeft = timeLeft,
        totalTime = restTime
    )
}

@Composable
fun CurrentExerciseContent(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise,
    timeLeft: Long,
    handleTimerAction: (Long) -> Unit,
    timerButtonText: String
) {
    val isExerciseWithTime = trainingExercise.time > 0L
    Column(modifier = modifier.fillMaxSize()) {
        if(isExerciseWithTime) {
            ExerciseWithTimeDescription(exercise = trainingExercise.exercise)
        } else {
            ExerciseWithoutTimeDescription(modifier = Modifier, exercise = trainingExercise.exercise)
        }
        Spacer(modifier = Modifier.height(16.dp))
        RepsAndSetsRow(modifier = Modifier, trainingExercise = trainingExercise)
        Spacer(modifier = Modifier.height(16.dp))
        if(isExerciseWithTime) {
            TrainingExerciseTimer(
                modifier = Modifier,
                timeLeft = timeLeft,
                totalTime = trainingExercise.time,
                handleTimerAction = handleTimerAction,
                timerButtonText = timerButtonText
            )
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(64.dp)) {
                ImageWithDefaultPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    imageDescription = "Exercise image",
                    image = trainingExercise.exercise.image
                )
            }
        }
    }
}

@Composable
fun ExerciseWithoutTimeDescription(modifier: Modifier, exercise: Exercise) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = exercise.name,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
        if(exercise.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = exercise.description, fontSize = 18.sp)
        }
    }

}

@Composable
fun TrainingExerciseTimer(
    modifier: Modifier = Modifier,
    timeLeft: Long,
    totalTime: Long,
    handleTimerAction: (Long) -> Unit,
    timerButtonText: String
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimerWithProgressBar(
            modifier = Modifier.weight(3f),
            timeLeft = timeLeft,
            totalTime = totalTime
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(
                modifier = Modifier,
                onClick = { handleTimerAction(totalTime) }
            ) {
                Text(text = timerButtonText, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }

}

@Composable
private fun TimerWithProgressBar(modifier: Modifier = Modifier, timeLeft: Long, totalTime: Long) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator( // TODO text is not in the center, the progress bar is heigher than box - setting size is not the best solution
            modifier = Modifier.size(300.dp),
            strokeWidth = 20.dp,
            progress = ((timeLeft - TimeFormatter.MILLIS_IN_SECOND).toFloat() / totalTime.toFloat())
        )
        Text(text = TimeFormatter.millisToTime(timeLeft), fontSize = 48.sp)
    }
}

@Composable
fun RepsAndSetsRow(modifier: Modifier, trainingExercise: TrainingExercise) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TrainingExerciseNumbers(modifier = Modifier, label = "Reps", value = trainingExercise.repetitions)
        TrainingExerciseNumbers(modifier = Modifier, label = "Sets", value = trainingExercise.sets)
    }
}

@Composable
private fun TrainingExerciseNumbers(modifier: Modifier = Modifier, label: String, value: Int) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = MaterialTheme.colors.primaryVariant)
        Text(text = value.toString(), fontSize = 32.sp, color = MaterialTheme.colors.primary)
    }
}

@Composable
fun ExerciseWithTimeDescription(modifier: Modifier = Modifier, exercise: Exercise) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(8.dp)) {
        ImageWithDefaultPlaceholder(
            modifier = Modifier.padding(8.dp),
            imageDescription = "Exercise image",
            image = exercise.image
        )
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = exercise.name,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary
            )
            if(exercise.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = exercise.description, fontSize = 18.sp)
            }
        }
    }
}


@Preview
@Composable
fun CurrentRestTimePrev() {
    CurrentRestTimeContent(restTime = 10000L, timeLeft = 9000L)
}