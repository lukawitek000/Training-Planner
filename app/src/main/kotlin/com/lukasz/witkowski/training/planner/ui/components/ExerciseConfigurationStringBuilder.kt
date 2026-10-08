package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R
import kotlin.time.Duration

@Composable
fun buildStringOverview(
    sets: Int,
    reps: Int,
    restTime: Duration,
    weightInKg: Int?
): String =
    if (weightInKg != null) {
        stringResource(
            R.string.recommendation_overview,
            sets,
            reps,
            restTime.inWholeSeconds,
            weightInKg
        )
    } else {
        stringResource(
            R.string.recommendation_no_weight,
            sets,
            reps,
            restTime.inWholeSeconds,
        )
    }
