package com.lukasz.witkowski.training.planner.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun TrainingPlannerTheme2(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TrainingPlannerColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
