package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RestTimeScreen(
    modifier: Modifier = Modifier,
    restTime: Long
) {
    Column {
        Text(text = "Rest time")
        Text(text = restTime.toString())
    }

}