package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun FailureScreen(
    title: String,
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(Dimens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.large))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
        }

    }
}

@Composable
@Preview
fun FailureScreenPreview() {
    TrainingPlannerTheme {
        FailureScreen(
            modifier = Modifier.fillMaxSize(),
            title = "Failure when loading some data",
            message = "Unknown failures"
        )
    }
}