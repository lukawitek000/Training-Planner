package com.lukasz.witkowski.training.planner.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.training.models.TrainingPlan
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