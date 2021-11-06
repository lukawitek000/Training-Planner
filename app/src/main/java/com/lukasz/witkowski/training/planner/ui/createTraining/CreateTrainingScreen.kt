package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.components.TextField

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.createTraining()
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Create, contentDescription = "Create training")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                text = title,
                onTextChange = { viewModel.onTrainingTitleChanged(it) },
                label = "Title",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = description,
                onTextChange = { viewModel.onTrainingDescriptionChanged(it) },
                label = "Description",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}