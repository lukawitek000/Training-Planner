package com.lukasz.witkowski.training.planner.training.trainingSession.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.TimeFormatter


@Composable
fun TimerWithCircularProgressBar(
    modifier: Modifier = Modifier,
    totalTime: Time,
    timeLeft: Time
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1.0f),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = timeLeft.timeInMillis / totalTime.timeInMillis.toFloat(),
            strokeWidth = 24.dp
        )
        Text(text = TimeFormatter(LocalContext.current).formatTime(timeLeft), fontSize = 42.sp)
    }
}