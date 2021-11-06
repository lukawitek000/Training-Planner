package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem


@Composable
fun TrainingsScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: TrainingsListViewModel,
    onCreateTrainingFabClicked: () -> Unit = {}
){
    val trainings = remember { viewModel.getAllTrainings() }
    Scaffold(
        modifier = Modifier.padding(innerPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateTrainingFabClicked() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Training")
            }
        }
    ) {
        LazyColumn(
            contentPadding = innerPadding
        ) {
            items(trainings) { training ->
                ListCardItem() {
                    TrainingListItemContent(training = training)
                }
            }
        }
    }
}


@Composable
fun TrainingListItemContent(
    modifier: Modifier = Modifier,
    training: Training
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = training.title,
                fontSize = 24.sp
            )
            Text(
                text = training.description,
                fontSize = 14.sp
            )
        }
        Icon(
            modifier = Modifier.size(40.dp),
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Start training",
            tint = Color.Yellow,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TrainingsScreenPreview() {
    TrainingsScreen(viewModel = hiltViewModel<TrainingsListViewModel>())
}