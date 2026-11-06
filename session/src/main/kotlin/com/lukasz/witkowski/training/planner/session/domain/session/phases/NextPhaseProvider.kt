package com.lukasz.witkowski.training.planner.session.domain.session.phases

import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueue
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder

internal class NextPhaseProvider(
    private val exerciseQueue: ExerciseQueue,
    private val statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder(),
) {
    fun nextPhase(currentPhase: SessionPhase): SessionPhase = when (currentPhase) {
        is IdlePhase, is RestPhase -> {
            if (exerciseQueue.isEmpty()) {
                SummaryPhase()
            } else {
                ExercisePhase(
                    loadedExercise = exerciseQueue.poll(),
                    statisticsRecorder = statisticsRecorder
                )
            }
        }
        is ExercisePhase -> {
            val nextExercise = exerciseQueue.peek()
            if (nextExercise == null) {
                SummaryPhase()
            } else {
                RestPhase(
                    restTime = currentPhase.restTime,
                    nextExercise = nextExercise
                )
            }
        }
        is SummaryPhase -> currentPhase
        else -> throw IllegalStateException("Invalid phase: $currentPhase")
    }
}