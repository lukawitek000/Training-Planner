package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TrainingOverviewScreen(
    modifier: Modifier,
    viewModel: TrainingOverviewViewModel,
    navigateBack: () -> Unit
) {
    Scaffold(modifier = modifier) {
        TrainingOverviewContent(modifier = Modifier)

    }
}

@Composable
fun TrainingOverviewContent(modifier: Modifier = Modifier) {
    Column() {
        Text(text = "Training overview")
    }
}

@Preview
@Composable
fun TrainingOverviewContentPreview() {
    TrainingOverviewContent()
}

