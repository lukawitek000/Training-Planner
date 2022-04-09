package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
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
        startRecordingTrainingStatistics(trainingPlan.id)
        loadSet(currentSet)
        loadExercise()
    }

    fun next(isCompleted: Boolean = true) {
        if (isCurrentStateExercise()) {
            saveExerciseAttemptStatistics(isCompleted)
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
            startRecordingExerciseStatistics(loadedExercise, currentSet)
        } else {
            currentSet++
            loadSet(currentSet)
            loadExercise()
        }
    }

    private fun setSummaryState() {
        val statistics = gatherTrainingStatistics()
        trainingSessionState.value =
            TrainingSessionState.SummaryState(statistics)
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

    // TODO separate recording statistics

    private var trainingPlanId: TrainingPlanId? = null
    private var startTrainingTime = Time.NONE
    private var exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private var currentExerciseStartTime = Time.NONE
    private var currentExerciseId: TrainingExerciseId? = null
    private var currentExerciseSet: Int = 0

    private fun startRecordingTrainingStatistics(trainingPlanId: TrainingPlanId) {
        startTrainingTime = getCurrentTime()
        this.trainingPlanId = trainingPlanId
    }

    private fun startRecordingExerciseStatistics(trainingExercise: TrainingExercise, set: Int) {
        currentExerciseStartTime = getCurrentTime()
        currentExerciseId = trainingExercise.id
        currentExerciseSet = set
    }

    private fun saveExerciseAttemptStatistics(completed: Boolean) {
        val exerciseAttemptStatistics = ExerciseAttemptStatistics(
            trainingExerciseId = currentExerciseId!!,
            time = getCurrentTime().minus(currentExerciseStartTime),
            set = currentExerciseSet,
            completed = completed
        )
        exercisesAttemptsStatistics.add(exerciseAttemptStatistics)
    }

    private fun gatherTrainingStatistics(): TrainingStatistics {
        val exercisesStatistics = exercises.map { exercise ->
            ExerciseStatistics(
                trainingExerciseId = exercise.id,
                attemptsStatistics = exercisesAttemptsStatistics.filter { exerciseAttemptStatistics -> exerciseAttemptStatistics.trainingExerciseId == exercise.id }
            )
        }
        return TrainingStatistics(
            trainingPlanId = trainingPlanId!!,
            totalTime = getCurrentTime().minus(startTrainingTime),
            exercisesStatistics = exercisesStatistics
        )
    }

    private fun getCurrentTime(): Time{
        val currentTimeInMillis = System.currentTimeMillis()
        return Time(currentTimeInMillis)
    }

}
