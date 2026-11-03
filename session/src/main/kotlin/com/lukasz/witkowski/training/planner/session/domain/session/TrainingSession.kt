package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.CompositeStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
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
    private val statisticsRecorder: SessionStatisticsRecorder = CompositeStatisticsRecorder(),
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {
    private val exercises = mutableListOf<TrainingExercise>()
    private val attempts = mutableListOf<ExerciseAttemptStatistics>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState

    private lateinit var exerciseSession: ExerciseSession
    private lateinit var statisticsBuilder: SessionStatisticsBuilder
    private var lastRecordedAttempt: ExerciseAttemptStatistics? = null

    init {
        require(trainingPlan.exercises.isNotEmpty()) {
            "Cannot start training session without exercises"
        }
        exercises.addAll(trainingSetsPolicy.loadExercises(trainingPlan))
    }

    fun start(startTime: Time): TrainingSessionState {
        statisticsRecorder.onEvent(SessionEvent.SessionStarted(trainingPlan, startTime))
        statisticsBuilder = SessionStatisticsBuilder(trainingPlan, startTime, timeProvider)
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
            val updatedTotalSets = getTotalSetsForExercise(currentExercise)
            statisticsRecorder.onEvent(
                SessionEvent.ExtraSetAdded(
                    currentExercise,
                    updatedTotalSets
                )
            )
            val updatedState = createExerciseState(currentExercise, currentSet)
            state = updatedState
            return updatedState
        }
        return state
    }

    fun updateLastAttemptRpe(rpe: Float) {
        lastRecordedAttempt?.let { attempt ->
            val updated = attempt.copy(rpe = rpe)
            val index = attempts.indexOfLast { it.id == attempt.id }
            if (index >= 0) {
                attempts[index] = updated
            }
            lastRecordedAttempt = updated
        }
    }

    fun finalizeStatistics(note: String? = null, rpe: Int? = null): TrainingStatistics {
        val summaryState = state as? TrainingSessionState.SummaryState
            ?: throw IllegalStateException("Cannot finalize session that is not in SummaryState")
        return summaryState.statistics.copy(note = note, rpe = rpe)
    }

    fun stop() {
        exercises.clear()
        attempts.clear()
        state = TrainingSessionState.IdleState
    }

    private fun next(time: Time): TrainingSessionState = when {
        isTrainingSessionFinished() && !isTrainingSessionStopped() -> {
            val stats = this.statisticsBuilder.gatherTrainingStatistics(time)
            statisticsRecorder.onEvent(SessionEvent.SessionFinished(stats.totalTime, time))
            TrainingSessionState.SummaryState(stats, trainingPlan)
        }
        isExerciseState() && exerciseSession.hasRestTime() -> {
            val nextExercise = getNextExerciseOverview()
            val nextSet = getNextExerciseSet(nextExercise)
            val following = getFollowingExercise(nextExercise)
            statisticsRecorder.onEvent(SessionEvent.RestStarted(exerciseSession.restTime, time))
            TrainingSessionState.RestTimeState(
                nextExercise = nextExercise,
                restTime = exerciseSession.restTime,
                nextSet = nextSet,
                totalSetsForNextExercise = getTotalSetsForExercise(nextExercise),
                lastCompletedAttempt = lastRecordedAttempt,
                followingExercise = following
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
        val following = getFollowingExercise(currentExercise)
        val totalSets = getTotalSetsForExercise(currentExercise)

        return TrainingSessionState.ExerciseState(
            currentExercise = currentExercise,
            currentSet = currentSet,
            totalSets = totalSets,
            targetReps = currentExercise.repetitions,
            targetWeightInKg = currentExercise.weightInKg?.toDouble(),
            completedSets = exerciseCompletedSets,
            exerciseIndex = if (exerciseIndex > 0) exerciseIndex else 1,
            totalExercises = totalExercises,
            setsLeftInWorkout = setsLeft,
            followingExercise = following
        )
    }

    private fun getTotalSetsForExercise(exercise: TrainingExercise): Int {
        val completed = attempts.count { it.trainingExerciseId == exercise.id }
        val remaining = exercises.count { it.id == exercise.id }
        val isInProgress = ::exerciseSession.isInitialized &&
            exerciseSession.exercise.id == exercise.id &&
            isExerciseState()
        return completed + remaining + (if (isInProgress) 1 else 0)
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
            statisticsBuilder.addExerciseAttemptStatistics(exerciseAttemptStatistics)
            statisticsRecorder.onEvent(
                SessionEvent.ExerciseCompleted(exerciseAttemptStatistics, time)
            )
        }
    }

    private fun getNextExerciseOverview() = exercises.first()

    private fun getNextExerciseSet(nextExercise: TrainingExercise): Int =
        attempts.count { it.trainingExerciseId == nextExercise.id } + 1

    private fun loadExercise(): TrainingExercise = exercises.removeAt(0)

    private fun getCurrentSet(currentExercise: TrainingExercise): Int =
        attempts.count { it.trainingExerciseId == currentExercise.id } + 1

    private fun getFollowingExercise(currentExercise: TrainingExercise): TrainingExercise? {
        val remaining = exercises.firstOrNull { it.id != currentExercise.id }
        if (remaining != null) return remaining
        return trainingPlan.exercises
            .dropWhile { it.id != currentExercise.id }
            .drop(1)
            .firstOrNull()
    }

    private fun isTrainingSessionFinished(): Boolean = exercises.isEmpty()

    private fun isRestTimeState() = state is TrainingSessionState.RestTimeState

    private fun isExerciseState() = state is TrainingSessionState.ExerciseState

    private fun isTrainingSessionStopped() = state is TrainingSessionState.IdleState
}
