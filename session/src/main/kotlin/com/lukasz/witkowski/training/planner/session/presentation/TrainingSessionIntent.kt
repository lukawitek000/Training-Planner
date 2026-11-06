package com.lukasz.witkowski.training.planner.session.presentation

import kotlin.time.Duration

sealed interface TrainingSessionIntent {
    sealed interface RepsChanged: TrainingSessionIntent {
        val changedBy: Int
        data class RepsIncreased(override val changedBy: Int): RepsChanged
        data class RepsDecreased(override val changedBy: Int): RepsChanged
    }
    sealed interface SetsChanged: TrainingSessionIntent {
        val changedBy: Int
        data class SetsIncreased(override val changedBy: Int): SetsChanged
        data class SetsDecreased(override val changedBy: Int): SetsChanged
    }
    sealed interface WeightChanged: TrainingSessionIntent {
        val changedBy: Double
        data class WeightIncreased(override val changedBy: Double): WeightChanged
        data class WeightDecreased(override val changedBy: Double): WeightChanged
    }
    sealed interface HoldTimeChanged: TrainingSessionIntent {
        val changedBy: Duration
        data class WeightIncreased(override val changedBy: Duration): HoldTimeChanged
        data class WeightDecreased(override val changedBy: Duration): HoldTimeChanged
    }
    data object CompleteSet: TrainingSessionIntent
    data object SkipSet: TrainingSessionIntent

}