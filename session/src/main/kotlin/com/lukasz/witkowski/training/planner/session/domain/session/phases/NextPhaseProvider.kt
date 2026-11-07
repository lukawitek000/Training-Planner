package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import kotlin.time.Duration

internal class NextPhaseProvider(
    private val exerciseQueue: ExerciseQueue,
    private val statisticsProvider: SessionStatisticsProvider,
    private val timerFactory: TimerFactory = TimerFactory()
) {
    fun nextPhase(currentPhase: SessionPhase): SessionPhase = when (currentPhase) {
        is IdlePhase, is RestPhase -> {
            if (exerciseQueue.isEmpty()) {
                SummaryPhase(statisticsProvider)
            } else {
                ExercisePhase(
                    loadedExercise = exerciseQueue.poll(),
                    timerFactory = timerFactory
                )
            }
        }

        is ExercisePhase -> {
            val nextExercise = exerciseQueue.peek()
            when {
                nextExercise == null -> SummaryPhase(statisticsProvider)
                currentPhase.restTime <= Duration.ZERO ->
                    ExercisePhase(
                        loadedExercise = exerciseQueue.poll(),
                        timerFactory = timerFactory
                    )
                else ->
                    RestPhase(
                        initialRestTime = currentPhase.restTime,
                        nextExercise = nextExercise,
                        timerFactory = timerFactory
                    )
            }
        }

        is SummaryPhase -> currentPhase
        else -> throw IllegalStateException("Invalid phase: $currentPhase")
    }

    fun summaryPhase(): SummaryPhase = SummaryPhase(statisticsProvider)
}
