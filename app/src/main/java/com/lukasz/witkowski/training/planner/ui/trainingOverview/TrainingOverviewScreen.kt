package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.shared.models.Training

@Composable
fun TrainingOverviewScreen(
    modifier: Modifier,
    viewModel: TrainingOverviewViewModel,
    navigateBack: () -> Unit
) {
    Scaffold(modifier = modifier) {
        TrainingOverviewContent(
            modifier = Modifier,
            training = viewModel.training
        )

    }
}

@Composable
fun TrainingOverviewContent(
    modifier: Modifier = Modifier,
    training: Training
) {
    Column(modifier = modifier
        .fillMaxHeight().padding(8.dp)// TODO here will be used inner padding
        .background(Color.Green)
    ) {
        if(training.description.isNotEmpty()) {
            Text(text = training.description, color = Color.Red)
        }
    }
}

@Preview
@Composable
fun TrainingOverviewContentPreview() {
    TrainingOverviewContent(
        training = Training(
            title = "Training title",
            description = "Training description can be long aisuhlkjbfl dsfh lkj ;foasihfl kjhlfkjh lakjfhalskfhlk jgdfl jkgdfkjhsd"
        )
    )
}

