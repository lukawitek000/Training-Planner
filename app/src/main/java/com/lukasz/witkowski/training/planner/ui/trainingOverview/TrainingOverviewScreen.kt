package com.lukasz.witkowski.training.planner.ui.trainingOverview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen

@Composable
fun TrainingOverviewScreen(
    modifier: Modifier,
    viewModel: TrainingOverviewViewModel,
    navigateBack: () -> Unit
) {
    val trainingRequest by viewModel.training.collectAsState()

    Scaffold(modifier = modifier) {
        when(trainingRequest) {
            is ResultHandler.Loading -> { LoadingScreen(Modifier.fillMaxSize()) }
            is ResultHandler.Success -> {
                TrainingOverviewContent(
                    modifier = Modifier,
                    trainingWithExercises = (trainingRequest as ResultHandler.Success<TrainingWithExercises>).value
                )
            }
            is ResultHandler.Error ->  {
                Toast.makeText(LocalContext.current, (trainingRequest as ResultHandler.Error).message, Toast.LENGTH_SHORT).show()
                navigateBack()
            }
        }

    }
}



@Composable
fun TrainingOverviewContent(
    modifier: Modifier = Modifier,
    trainingWithExercises: TrainingWithExercises
) {
    val training = trainingWithExercises.training
    Column(modifier = modifier
        .fillMaxSize()
        .padding(8.dp)
        .background(Color.Green)
    ) {
        Text(training.title)
        if(training.description.isNotEmpty()) {
            Text(text = training.description, color = Color.Red)
        }
    }
}


