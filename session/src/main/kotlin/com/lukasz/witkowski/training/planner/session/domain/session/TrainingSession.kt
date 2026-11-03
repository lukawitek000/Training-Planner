package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

/**
 * Training session aggregate - controls state and progression of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
@Suppress("TooManyFunctions")
internal class TrainingSession(
    private val trainingPlan: TrainingPlan,
    private val trainingSetsPolicy: TrainingSetsPolicy = StraightSetsPolicy(),
    private val statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder()
) {
    private val exercises = mutableListOf<TrainingExercise>()
    private val attempts = mutableListOf<ExerciseAttemptStatistics>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState

    private lateinit var exerciseSession: ExerciseSession
    private lateinit var trainingStatistics: TrainingStatistics
    private var lastRecordedAttempt: ExerciseAttemptStatistics? = null

    init {
        require(trainingPlan.exercises.isNotEmpty()) {
            "Cannot start training session without exercises"
        }
        exercises.addAll(trainingSetsPolicy.loadExercises(trainingPlan))
    }

    fun start(startTime: Time): TrainingSessionState {
        statisticsRecorder.onEvent(SessionEvent.SessionStarted(trainingPlan, startTime))
        trainingStatistics = TrainingStatistics(trainingPlan, startTime)
        val firstExercise = loadExercise()
        val currentSet = getCurrentSet(firstExercise)
        exerciseSession = ExerciseSession(firstExercise, startTime, currentSet)
        statisticsRecorder.onEvent(
            SessionEvent.ExerciseStarted(firstExercise, currentSet, startTime)
        )
        return createExerciseState(firstExercise, currentSet).also {
            state = it
        }
    }

    fun skip(time: Time): TrainingSessionState {
        if (isRestTimeState()) {
            statisticsRecorder.onEvent(SessionEvent.RestSkipped(time))
        }
        stopRecordingExerciseStatistics(isCompleted = false, time = time)
        return next(time)
    }

    fun completed(
        time: Time,
        actualReps: Int? = null,
        actualWeightInKg: Double? = null,
        rpe: Float? = null,
        actualTime: Duration? = null
    ): TrainingSessionState {
        if (isRestTimeState()) {
            statisticsRecorder.onEvent(SessionEvent.RestCompleted(time))
        }
        stopRecordingExerciseStatistics(
            isCompleted = true,
            time = time,
            actualReps = actualReps,
            actualWeightInKg = actualWeightInKg,
            rpe = rpe,
            actualTime = actualTime
        )
        return next(time)
    }

    fun skipExercise(time: Time): TrainingSessionState {
        if (state is TrainingSessionState.ExerciseState) {
            val currentExercise = exerciseSession.exercise
            statisticsRecorder.onEvent(
                SessionEvent.ExerciseSkipped(currentExercise, exerciseSession.set, time)
            )
            val currentExerciseId = currentExercise.id
            exercises.removeAll { it.id == currentExerciseId }
            stopRecordingExerciseStatistics(isCompleted = false, time = time)
            return next(time)
        }
        return state
    }

    fun addExtraSet(): TrainingSessionState {
        if (state is TrainingSessionState.ExerciseState) {
            val currentExercise = exerciseSession.exercise
            val insertIndex = exercises.indexOfLast { it.id == currentExercise.id }.let {
                if (it >= 0) it + 1 else 0
            }
            exercises.add(insertIndex, currentExercise)
            val currentSet = exerciseSession.set
            statisticsRecorder.onEvent(
                SessionEvent.ExtraSetAdded(
                    currentExercise,
                    exercises.count { it.id == currentExercise.id } + currentSet
                )
            )
            val updatedState = createExerciseState(currentExercise, currentSet)
            state = updatedState
            return updatedState
        }
        return state
    }

    fun stop() {
        exercises.clear()
        attempts.clear()
        state = TrainingSessionState.IdleState
    }

    private fun next(time: Time): TrainingSessionState = when {
        isTrainingSessionFinished() && !isTrainingSessionStopped() -> {
            val stats = this.trainingStatistics.gatherTrainingStatistics(time)
            statisticsRecorder.onEvent(SessionEvent.SessionFinished(stats.totalTime, time))
            TrainingSessionState.SummaryState(stats, trainingPlan)
        }
        isExerciseState() && exerciseSession.hasRestTime() -> {
            val nextExercise = getNextExerciseOverview()
            val nextSet = getNextExerciseSet(nextExercise)
            val followingName = getFollowingExerciseName(nextExercise)
            statisticsRecorder.onEvent(SessionEvent.RestStarted(exerciseSession.restTime, time))
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                restTime = exerciseSession.restTime,
                nextSet = nextSet,
                totalSetsForNextExercise = nextExercise.sets,
                lastCompletedAttempt = lastRecordedAttempt,
                followingExerciseName = followingName
            )
        }
        isRestTimeState() || (isExerciseState() && !exerciseSession.hasRestTime()) -> {
            val currentExercise = loadExercise()
            val currentSet = getCurrentSet(currentExercise)
            startRecordingExerciseStatistics(currentExercise, time, currentSet)
            statisticsRecorder.onEvent(
                SessionEvent.ExerciseStarted(currentExercise, currentSet, time)
            )
            createExerciseState(currentExercise, currentSet)
        }
        else -> throw UnknownTrainingSessionStateException("Unknown training session state.")
    }.also { state = it }

    private fun createExerciseState(
        currentExercise: TrainingExercise,
        currentSet: Int
    ): TrainingSessionState.ExerciseState {
        val exerciseCompletedSets = attempts.filter { it.trainingExerciseId == currentExercise.id }
        val exerciseIndex = trainingPlan.exercises.indexOfFirst { it.id == currentExercise.id } + 1
        val totalExercises = trainingPlan.exercises.size
        val setsLeft = exercises.size
        val followingName = getFollowingExerciseName(currentExercise)

        return TrainingSessionState.ExerciseState(
            currentExercise = currentExercise,
            currentSet = currentSet,
            totalSets = currentExercise.sets,
            targetReps = currentExercise.repetitions,
            targetWeightInKg = currentExercise.weightInKg?.toDouble(),
            completedSets = exerciseCompletedSets,
            exerciseIndex = if (exerciseIndex > 0) exerciseIndex else 1,
            totalExercises = totalExercises,
            setsLeftInWorkout = setsLeft,
            followingExerciseName = followingName
        )
    }

    private fun startRecordingExerciseStatistics(
        currentExercise: TrainingExercise,
        time: Time,
        currentSet: Int
    ) {
        exerciseSession = ExerciseSession(currentExercise, time, currentSet)
    }

    private fun stopRecordingExerciseStatistics(
        isCompleted: Boolean,
        time: Time,
        actualReps: Int? = null,
        actualWeightInKg: Double? = null,
        rpe: Float? = null,
        actualTime: Duration? = null
    ) {
        if (isExerciseState()) {
            val currentExercise = exerciseSession.exercise
            val reps = actualReps ?: currentExercise.repetitions
            val weight = actualWeightInKg ?: currentExercise.weightInKg?.toDouble()
            val exerciseAttemptStatistics = exerciseSession.stop(
                isCompleted = isCompleted,
                stopTime = time,
                actualReps = reps,
                actualWeightInKg = weight,
                rpe = rpe,
                overrideDuration = actualTime
            )
            attempts.add(exerciseAttemptStatistics)
            lastRecordedAttempt = exerciseAttemptStatistics
            trainingStatistics.addExerciseAttemptStatistics(exerciseAttemptStatistics)
            statisticsRecorder.onEvent(
                SessionEvent.ExerciseCompleted(exerciseAttemptStatistics, time)
            )
        }
    }

    private fun getNextExerciseOverview() = exercises.first()

    private fun getNextExerciseSet(nextExercise: TrainingExercise): Int {
        val remainingAttempts = exercises.count { it.id == nextExercise.id }
        return nextExercise.sets - remainingAttempts + 1
    }

    private fun loadExercise(): TrainingExercise = exercises.removeAt(0)

    private fun getCurrentSet(currentExercise: TrainingExercise): Int {
        val numberOfLeftAttempts = exercises.count { it.id == currentExercise.id }
        return currentExercise.sets - numberOfLeftAttempts
    }

    private fun getFollowingExerciseName(currentExercise: TrainingExercise): String? {
        val followingExercise = exercises.firstOrNull { it.id != currentExercise.id }
            ?: trainingPlan.exercises.dropWhile {
                it.id != currentExercise.id
            }.drop(1).firstOrNull()
        return followingExercise?.let { "${it.exercise.name} (${it.sets} sets)" }
    }

    private fun isTrainingSessionFinished(): Boolean = exercises.isEmpty()

    private fun isRestTimeState() = state is TrainingSessionState.RestTimeState

    private fun isExerciseState() = state is TrainingSessionState.ExerciseState

    private fun isTrainingSessionStopped() = state is TrainingSessionState.IdleState
}
