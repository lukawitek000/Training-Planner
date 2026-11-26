package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TrainingNotificationActionReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val trainingSessionService: TrainingSessionService by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        val action = TrainingNotificationAction.fromIntentAction(intent?.action) ?: return
        val currentState = trainingSessionService.trainingSessionState.value
        if (currentState is TrainingSessionState.IdleState) {
            Timber.w("Ignoring notification action %s for idle session", action)
            return
        }
        when (action) {
            TrainingNotificationAction.SKIP -> trainingSessionService.skip()
            TrainingNotificationAction.COMPLETE -> trainingSessionService.completed()
            TrainingNotificationAction.START_TIMER -> {
                if (
                    currentState is TrainingSessionState.ExerciseState &&
                    currentState.timerState is TimerState.Ready
                ) {
                    trainingSessionService.startTimer()
                }
            }
            TrainingNotificationAction.RESET_TIMER -> {
                if (currentState is TrainingSessionState.ExerciseState) {
                    trainingSessionService.resetTimer()
                }
            }
        }
    }
}
