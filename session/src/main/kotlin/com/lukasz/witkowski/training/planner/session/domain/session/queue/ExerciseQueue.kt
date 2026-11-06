package com.lukasz.witkowski.training.planner.session.domain.session.queue

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.phases.SessionPhase
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class ExerciseQueue(
    private val loadedExercises: List<SessionExercise>,
) {
    init {
        require(loadedExercises.isNotEmpty()) {
            "Cannot initialize exercises queue without any exercise"
        }
    }
    private val groupedLoadedExercises = loadedExercises.groupBy { it.exerciseId }

    private val remainingExercises = loadedExercises.toMutableList()
    val progress: StateFlow<ExerciseQueueProgress>
        field = MutableStateFlow(calculateProgress(loadedExercises.first().exerciseId))

    fun isEmpty() = remainingExercises.isEmpty()

    fun poll(): SessionExercise {
        check(!isEmpty()) {
            "Exercise queue is empty, cannot load next exercise"
        }
        return remainingExercises.removeAt(0).also {
            updateProgress(it.exerciseId)
        }
    }

    fun peek(): SessionExercise? {
        return remainingExercises.getOrNull(0)
    }

    private fun updateProgress(currentExerciseId: TrainingExerciseId) {
        progress.update { calculateProgress(currentExerciseId) }
    }

    private fun calculateProgress(currentExerciseId: TrainingExerciseId): ExerciseQueueProgress {
        val groupedExercises = remainingExercises.groupBy { it.exerciseId }
        return ExerciseQueueProgress(
            remainingExerciseSets = groupedExercises[currentExerciseId]!!.size,
            currentExerciseTotalSets = groupedLoadedExercises[currentExerciseId]!!.size,
            remainingExercises = groupedExercises.size,
            totalExercises = groupedLoadedExercises.size,
            remainingSetsInWorkout = remainingExercises.size,
        )
    }
}