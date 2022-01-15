package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.service.PhoneTrainingService
import com.lukasz.witkowski.training.planner.ui.exercisesList.ImageWithDefaultPlaceholder

@Composable
fun CurrentTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CurrentTrainingViewModel,
    navigateBack: (String) -> Unit,
    trainingService: PhoneTrainingService
) {
    val trainingFetchState by viewModel.trainingFetchState.collectAsState(initial = ResultHandler.Loading)
    val currentTrainingState by trainingService.trainingProgressController.currentTrainingState.observeAsState()
    Scaffold(modifier = modifier.fillMaxSize()) {
        Text(text = "Training id ${viewModel.trainingId}")
        when(trainingFetchState) {
            is ResultHandler.Loading -> {
                LoadingTrainingContent()
            }
            is ResultHandler.Success -> {
                currentTrainingState?.let { it1 ->
                    CurrentTrainingContent(
                        modifier = Modifier,
                        currentTrainingState = it1,
                        navigateFurther = { trainingService.trainingProgressController.navigateToTheNextScreen() }
                    )
                }
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
    navigateFurther: () -> Unit
) {
    Column() {
        when(currentTrainingState) {
            is CurrentTrainingState.ExerciseState -> {
                CurrentExerciseContent(modifier = Modifier, trainingExercise = currentTrainingState.exercise)
            }
            is CurrentTrainingState.RestTimeState -> {
                CurrentRestTimeContent(modifier = Modifier, restTime = currentTrainingState.restTime)
            }
            is CurrentTrainingState.SummaryState -> {
                CurrentTrainingSummaryContent(modifier = Modifier)
            }
        }
        Button(onClick = navigateFurther) {
            Text(text = "Next")
        }
    }

}

@Composable
fun CurrentTrainingSummaryContent(modifier: Modifier) {
    Text(text = "Training summary")
}

@Composable
fun CurrentRestTimeContent(modifier: Modifier, restTime: Long) {
    Text("Rest time $restTime")
}

@Composable
fun CurrentExerciseContent(modifier: Modifier = Modifier, trainingExercise: TrainingExercise) {
    Column(modifier = modifier.fillMaxSize()) {
        ExerciseDescription(exercise = trainingExercise.exercise)
        Spacer(modifier = Modifier.height(16.dp))
        RepsAndSetsRow(modifier = Modifier, trainingExercise = trainingExercise)
        Spacer(modifier = Modifier.height(16.dp))
        TrainingTimer(modifier = Modifier, time = trainingExercise.time)
    }
}

@Composable
fun TrainingTimer(modifier: Modifier, time: Long) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = TimeFormatter.millisToTime(time))
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
fun ExerciseDescription(modifier: Modifier = Modifier, exercise: Exercise) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(8.dp)) {
        ImageWithDefaultPlaceholder(
            modifier = Modifier.padding(8.dp),
            imageDescription = "Exercise image",
            image = null
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
fun CurrentExercisePrev() {
    CurrentExerciseContent(modifier = Modifier, trainingExercise = TrainingExercise(
        id = 0L,
        trainingId = 2L,
        exercise = Exercise(
            name = "Push ups",
            description = " r errgerg erg erg erger esr wrt ht ju y kim tu jyk iyk iykt ",
            image = null
        ),
        sets = 3,
        repetitions = 15,
        time = 10000L,
        restTime = 60000L
    ))
}