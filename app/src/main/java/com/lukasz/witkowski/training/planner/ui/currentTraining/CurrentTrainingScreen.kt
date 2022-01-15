package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R

@Composable
fun CurrentTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CurrentTrainingViewModel,
    navigateBack: (String) -> Unit
) {
    val trainingFetchState by viewModel.trainingFetchState.collectAsState(initial = ResultHandler.Loading)

    Scaffold(modifier = modifier.fillMaxSize()) {
        Text(text = "Training id ${viewModel.trainingId}")
        when(trainingFetchState) {
            is ResultHandler.Loading -> {
                LoadingTrainingContent()
            }
            is ResultHandler.Success -> {
                CurrentTrainingContent(modifier = Modifier, trainingWithExercises = (trainingFetchState as ResultHandler.Success<TrainingWithExercises>).value)
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
    trainingWithExercises: TrainingWithExercises
) {
    Text(text = "Training \n $trainingWithExercises")
}

