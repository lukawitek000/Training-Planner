package com.lukasz.witkowski.training.planner.statistics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.trainingOverview.TrainingOverviewContent

@Composable
fun TrainingSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit
) {

    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
    Surface(modifier = modifier.fillMaxSize()) {
        when (trainingSessionState) {
            is TrainingSessionState.Idle -> LoadingScreen(Modifier.fillMaxSize())
            is TrainingSessionState.TrainingPlanLoadedState -> LoadedTrainingPlanOverview(
                trainingPlan = (trainingSessionState as TrainingSessionState.TrainingPlanLoadedState).trainingPlan,
                startTrainingSession = { viewModel.startTraining() }
            )
        }
    }
}

@Composable
fun LoadedTrainingPlanOverview(
    modifier: Modifier = Modifier,
    trainingPlan: TrainingPlan,
    startTrainingSession: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = startTrainingSession) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(id = R.string.start_training_session)
                )
            }
        }
    ) {
        TrainingOverviewContent(
            modifier = modifier,
            trainingPlan = trainingPlan,
            isTrainingExercisesExpandable = false
        )
    }
}