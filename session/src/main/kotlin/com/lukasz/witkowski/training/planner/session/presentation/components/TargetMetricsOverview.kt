package com.lukasz.witkowski.training.planner.session.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise

@Composable
fun TargetMetricsOverview(sessionExercise: SessionExercise) {
    val targetMetric =
        if (sessionExercise.currentConfig.isIsometric) {
            stringResource(
                R.string.session_target_hold_format,
                sessionExercise.snapshot.plannedConfig.holdTime!!.inWholeSeconds
            )
        } else {
            stringResource(
                R.string.session_target_reps_format,
                sessionExercise.snapshot.plannedConfig.reps
            )
        }
    val weight = sessionExercise.snapshot.plannedConfig.weightInKg
    val weightSuffix = if (weight != null && weight > 0) {
        stringResource(R.string.session_target_weight_suffix, weight.toString())
    } else {
        ""
    }
    val summary = stringResource(
        R.string.session_target_summary_format,
        targetMetric
    )
    Text(
        text = summary + weightSuffix,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
