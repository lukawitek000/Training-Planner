package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow

class TrainingSessionService {

    private lateinit var trainingPlan: TrainingPlan

    val trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)

    private var currentSet = 1
    private val exercises: List<TrainingExercise>
        get() = trainingPlan.exercises

    private val currentSetExercises = mutableListOf<TrainingExercise>()

    fun startTraining(trainingPlan: TrainingPlan) {
        this.trainingPlan = trainingPlan
        loadSet(currentSet)
        loadExercise()
    }

    fun next(isCompleted: Boolean = true) {
        if (isCurrentStateExercise()) {
            val nextExercise = getNextExercise()
            if (nextExercise != null) {
                setRestTimeState(nextExercise)
            } else {
                setSummaryState()
            }
        } else if (isCurrentStateRestTime()) {
            loadExercise()
        }
    }

    private fun loadSet(setNumber: Int) {
        currentSetExercises.clear()
        exercises.forEach { exercise ->
            if (exercise.sets >= setNumber) {
                currentSetExercises.add(exercise)
            }
        }
    }

    private fun loadExercise() {
        if (currentSetExercises.isNotEmpty()) {
            val loadedExercise = currentSetExercises.removeFirst()
            trainingSessionState.value = TrainingSessionState.ExerciseState(loadedExercise)
        } else {
            currentSet++
            loadSet(currentSet)
            loadExercise()
        }
    }

    private fun setSummaryState() {
        trainingSessionState.value =
            TrainingSessionState.SummaryState("Training finished")
    }

    private fun getNextExercise(): TrainingExercise? {
        if (currentSetExercises.isEmpty()) {
            return exercises.firstOrNull { it.sets >= currentSet + 1 }
        }
        return currentSetExercises.first()
    }


    private fun setRestTimeState(nextExercise: TrainingExercise) {
        val restTime = trainingSessionState.value.exercise?.restTime ?: Time.NONE
        if (restTime.isNotZero()) {
            trainingSessionState.value = TrainingSessionState.RestTimeState(nextExercise, restTime)
        } else {
            loadExercise()
        }
    }


    private fun isCurrentStateExercise() =
        trainingSessionState.value is TrainingSessionState.ExerciseState

    private fun isCurrentStateRestTime() =
        trainingSessionState.value is TrainingSessionState.RestTimeState
}
