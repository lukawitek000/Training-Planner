package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
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
import com.lukasz.witkowski.shared.currentTraining.CurrentTrainingState
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.service.PhoneTrainingService

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
fun CurrentExerciseContent(modifier: Modifier, trainingExercise: TrainingExercise) {
    Text(text = "Current exercise $trainingExercise")
}

