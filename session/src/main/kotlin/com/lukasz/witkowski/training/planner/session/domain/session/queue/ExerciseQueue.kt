package com.lukasz.witkowski.training.planner.session.domain.session.queue

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class ExerciseQueue(loadedExercises: List<SessionExercise>) {
    init {
        require(loadedExercises.isNotEmpty()) {
            "Cannot initialize exercises queue without any exercise"
        }
    }

    private val totalSetsByExercise =
        loadedExercises
            .groupBy { it.exerciseId }
            .mapValues { it.value.size }
            .toMutableMap()

    private val totalDistinctExercises = totalSetsByExercise.size
    private val remainingExercises = loadedExercises.toMutableList()
    private var activeExercise: SessionExercise = loadedExercises.first()
    private var activeExerciseId: TrainingExerciseId = activeExercise.exerciseId

    val progress: StateFlow<ExerciseQueueProgress>
        field = MutableStateFlow(calculateProgress(activeExerciseId))

    fun isEmpty(): Boolean = remainingExercises.isEmpty()

    fun poll(): SessionExercise {
        check(!isEmpty()) {
            "Exercise queue is empty, cannot load next exercise"
        }
        return remainingExercises.removeAt(0).also { polled ->
            activeExercise = polled
            activeExerciseId = polled.exerciseId
        }
    }

    fun peek(): SessionExercise? = remainingExercises.firstOrNull()

    fun refreshProgress() {
        updateProgress(activeExerciseId)
    }

    fun addExtraSet(): Pair<SessionExercise, Int> {
        val exerciseId = activeExercise.exerciseId
        val insertIndex =
            remainingExercises.indexOfLast { it.exerciseId == exerciseId }.let { lastIndex ->
                if (lastIndex >= 0) lastIndex + 1 else 0
            }
        remainingExercises.add(insertIndex, activeExercise)
        val updatedTotal = (totalSetsByExercise[exerciseId] ?: 1) + 1
        totalSetsByExercise[exerciseId] = updatedTotal
        updateProgress(activeExerciseId)
        return activeExercise to updatedTotal
    }

    fun removeExtraSet(): Boolean {
        val index = remainingExercises.indexOfLast { it.exerciseId == activeExerciseId }
        if (index < 0) return false
        remainingExercises.removeAt(index)
        val currentTotal = totalSetsByExercise[activeExerciseId] ?: 1
        totalSetsByExercise[activeExerciseId] = (currentTotal - 1).coerceAtLeast(1)
        updateProgress(activeExerciseId)
        return true
    }

    fun updateConfigForRemaining(
        exerciseId: TrainingExerciseId,
        config: SessionExerciseConfiguration
    ) {
        if (activeExercise.exerciseId == exerciseId) {
            activeExercise = activeExercise.copy(currentConfig = config)
        }
        for (i in remainingExercises.indices) {
            if (remainingExercises[i].exerciseId == exerciseId) {
                remainingExercises[i] = remainingExercises[i].copy(currentConfig = config)
            }
        }
    }

    private fun updateProgress(currentExerciseId: TrainingExerciseId) {
        progress.update { calculateProgress(currentExerciseId) }
    }

    private fun calculateProgress(currentExerciseId: TrainingExerciseId): ExerciseQueueProgress {
        val groupedExercises = remainingExercises.groupBy { it.exerciseId }
        return ExerciseQueueProgress(
            remainingExerciseSets = groupedExercises[currentExerciseId]?.size ?: 0,
            currentExerciseTotalSets = totalSetsByExercise[currentExerciseId] ?: 0,
            remainingExercises = groupedExercises.size,
            totalExercises = totalDistinctExercises,
            remainingSetsInWorkout = remainingExercises.size
        )
    }
}
