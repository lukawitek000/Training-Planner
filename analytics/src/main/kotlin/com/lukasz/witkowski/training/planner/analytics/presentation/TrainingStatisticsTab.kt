package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.training.R

@Composable
internal fun TrainingStatisticsTab(
    modifier: Modifier = Modifier
) {
    AppEmptyState(
        title = stringResource(id = R.string.no_previous_sessions),
        modifier = modifier
    )
}

@AppPreview
@Composable
private fun TrainingStatisticsTabPreview() {
    TrainingPlannerTheme {
        TrainingStatisticsTab(
            modifier = Modifier.fillMaxSize()
        )
    }
}