package com.lukasz.witkowski.training.planner.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.util.Consumer
import com.lukasz.witkowski.training.planner.notification.AppSessionNotificationContentIntentProvider
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import org.koin.compose.koinInject

class AppNavigationIntentHandler(
    private val trainingSessionService: TrainingSessionService
) {
    fun handle(intent: Intent?, navigator: TrainingPlannerNavigator): Boolean {
        if (
            intent?.getBooleanExtra(
                AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION,
                false
            ) != true
        ) {
            return false
        }
        intent.removeExtra(AppSessionNotificationContentIntentProvider.EXTRA_OPEN_ACTIVE_SESSION)
        val isActive = trainingSessionService.trainingSessionState.value !is TrainingSessionState.IdleState
        val activePlanId = trainingSessionService.trainingPlan?.id
        if (!isActive || activePlanId == null) {
            return false
        }
        navigator.openActiveTrainingSession(activePlanId)
        return true
    }
}

@Composable
fun HandleNavigationIntents(navigator: TrainingPlannerNavigator) {
    if (LocalInspectionMode.current) return
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val intentHandler: AppNavigationIntentHandler = koinInject()
    val currentNavigator by rememberUpdatedState(navigator)

    LaunchedEffect(activity) {
        intentHandler.handle(activity.intent, currentNavigator)
    }

    DisposableEffect(activity) {
        val listener = Consumer<Intent> { newIntent ->
            activity.intent = newIntent
            intentHandler.handle(newIntent, currentNavigator)
        }
        activity.addOnNewIntentListener(listener)
        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }
}
