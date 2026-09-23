package com.lukasz.witkowski.training.planner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun TrainingPlannerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TrainingPlannerColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
