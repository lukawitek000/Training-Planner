package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import timber.log.Timber

class LoggingNavDecorator : NavEntryDecorator<TrainingPlannerNavKey>(
    decorate = { entry ->
        LaunchedEffect(entry) {
            Timber.i("Add NavEntry: ${entry.contentKey}")
        }
        entry.Content()
    },
    onPop = {
        Timber.i("Pop up NavEntry: $it")
    }
)

@Composable
fun rememberLoggingNavDecorator(): LoggingNavDecorator = remember {
    LoggingNavDecorator()
}
