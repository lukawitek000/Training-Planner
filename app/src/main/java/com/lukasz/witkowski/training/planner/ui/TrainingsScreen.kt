package com.lukasz.witkowski.training.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.shared.dummyTrainingList


@Composable
fun TrainingsScreen(){
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(text = "Trainings Screen",
            color = Color.Yellow
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TrainingsScreenPreview() {
    TrainingsScreen()
}