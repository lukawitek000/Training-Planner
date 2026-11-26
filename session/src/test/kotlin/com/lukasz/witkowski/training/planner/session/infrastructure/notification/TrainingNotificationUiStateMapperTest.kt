package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.createSessionExercise
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.kg
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import org.junit.Test

class TrainingNotificationUiStateMapperTest {
    private val mapper = TrainingNotificationUiStateMapper()

    @Test
    fun `maps IdleState to Idle`() {
        val result = mapper.map(TrainingSessionState.IdleState)

        assertEquals(TrainingNotificationUiState.Idle, result)
    }

    @Test
    fun `maps ExerciseState to ActiveExercise with reps, weight, and workout segments`() {
        val exercise =
            createSessionExercise(
                name = "Bench Press",
                reps = 10,
                weight = 60.kg
            )
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 2,
                currentExerciseTotalSets = 3,
                totalExercises = 2,
                remainingExercises = 2,
                remainingSetsInWorkout = 6,
                nextSetQueueProgress = null,
                exerciseSetCounts = listOf(3, 4)
            )

        val result =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.NotSet,
                    progress = progress
                )
            )

        assertEquals(
            TrainingNotificationUiState.ActiveExercise(
                exerciseName = "Bench Press",
                currentSet = 1,
                totalSets = 3,
                reps = 10,
                weight = 60.kg,
                weightUnit = WeightUnit.KG,
                holdSeconds = null,
                timer = NotificationTimer.None,
                progress =
                NotificationProgress.WorkoutSegments(
                    completedSets = 0,
                    exerciseSetCounts = listOf(3, 4)
                )
            ),
            result
        )
    }

    @Test
    fun `maps isometric ExerciseState with holdSeconds and completedSets progression`() {
        val exercise =
            createSessionExercise(
                name = "Plank",
                reps = 1,
                weight = null,
                holdTime = 45.seconds
            )
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 1,
                currentExerciseTotalSets = 4,
                totalExercises = 2,
                remainingExercises = 1,
                remainingSetsInWorkout = 1,
                nextSetQueueProgress = null,
                exerciseSetCounts = listOf(3, 4)
            )

        val result =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.Running(remaining = 14_200.milliseconds),
                    progress = progress
                )
            )

        assertEquals(
            TrainingNotificationUiState.ActiveExercise(
                exerciseName = "Plank",
                currentSet = 3,
                totalSets = 4,
                reps = 1,
                weight = null,
                weightUnit = WeightUnit.KG,
                holdSeconds = 45L,
                timer = NotificationTimer.Running(remainingSeconds = 15L),
                progress =
                NotificationProgress.WorkoutSegments(
                    completedSets = 5,
                    exerciseSetCounts = listOf(3, 4)
                )
            ),
            result
        )
    }

    @Test
    fun `falls back to exercise-level segments when exerciseSetCounts is empty`() {
        val exercise = createSessionExercise(name = "Pull Up")
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 1,
                currentExerciseTotalSets = 3,
                totalExercises = 3,
                remainingExercises = 1,
                remainingSetsInWorkout = 1,
                nextSetQueueProgress = null,
                exerciseSetCounts = emptyList()
            )

        val result =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.NotSet,
                    progress = progress
                )
            ) as TrainingNotificationUiState.ActiveExercise

        assertEquals(
            NotificationProgress.WorkoutSegments(
                completedSets = 2,
                exerciseSetCounts = listOf(1, 1, 1)
            ),
            result.progress
        )
    }

    @Test
    fun `maps RestTimeState with countdown progress and null when totalRestTime is zero`() {
        val nextExercise = createSessionExercise(name = "Squat")
        val nextProgress =
            ExerciseQueueProgress(
                remainingExerciseSets = 1,
                currentExerciseTotalSets = 4,
                totalExercises = 2,
                remainingExercises = 1,
                remainingSetsInWorkout = 1,
                nextSetQueueProgress = null
            )

        val runningRest =
            mapper.map(
                TrainingSessionState.RestTimeState(
                    nextExercise = nextExercise,
                    timerState = TimerState.Running(remaining = 39_100.milliseconds),
                    totalRestTime = 60.seconds,
                    nextSetQueueProgress = nextProgress
                )
            )

        assertEquals(
            TrainingNotificationUiState.Rest(
                nextExerciseName = "Squat",
                nextSet = 3,
                nextTotalSets = 4,
                timer = NotificationTimer.Running(remainingSeconds = 40L),
                progress =
                NotificationProgress.RestCountdown(
                    elapsedSeconds = 20,
                    totalSeconds = 60
                )
            ),
            runningRest
        )

        val finishedRest =
            mapper.map(
                TrainingSessionState.RestTimeState(
                    nextExercise = nextExercise,
                    timerState = TimerState.Finished,
                    totalRestTime = 60.seconds,
                    nextSetQueueProgress = null
                )
            )

        assertEquals(
            TrainingNotificationUiState.Rest(
                nextExerciseName = "Squat",
                nextSet = null,
                nextTotalSets = null,
                timer = NotificationTimer.Finished,
                progress =
                NotificationProgress.RestCountdown(
                    elapsedSeconds = 60,
                    totalSeconds = 60
                )
            ),
            finishedRest
        )

        val zeroRest =
            mapper.map(
                TrainingSessionState.RestTimeState(
                    nextExercise = nextExercise,
                    timerState = TimerState.NotSet,
                    totalRestTime = Duration.ZERO,
                    nextSetQueueProgress = null
                )
            )

        assertEquals(
            TrainingNotificationUiState.Rest(
                nextExerciseName = "Squat",
                nextSet = null,
                nextTotalSets = null,
                timer = NotificationTimer.None,
                progress = null
            ),
            zeroRest
        )
    }

    @Test
    fun `maps SummaryState to Summary with Completed progress`() {
        val summaryState =
            TrainingSessionState.SummaryState(
                statistics =
                TrainingStatistics(
                    trainingPlanId = TRAINING_PLAN.id,
                    totalTime = 1800.seconds,
                    date = Instant.fromEpochMilliseconds(0L),
                    exercisesStatistics = emptyList()
                )
            )

        val result = mapper.map(summaryState)

        assertEquals(
            TrainingNotificationUiState.Summary(
                progress = NotificationProgress.Completed
            ),
            result
        )
    }

    @Test
    fun `normalizes timer ticks to whole seconds for distinctUntilChanged deduplication`() {
        val exercise = createSessionExercise(name = "Plank")
        val progress =
            ExerciseQueueProgress(
                remainingExerciseSets = 1,
                currentExerciseTotalSets = 1,
                totalExercises = 1,
                remainingExercises = 1,
                remainingSetsInWorkout = 1,
                nextSetQueueProgress = null,
                exerciseSetCounts = listOf(1)
            )

        val tick1 =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.Running(1500.milliseconds),
                    progress = progress
                )
            )
        val tick2 =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.Running(1100.milliseconds),
                    progress = progress
                )
            )
        assertEquals(tick1, tick2)
        assertEquals(
            NotificationTimer.Running(2L),
            (tick1 as TrainingNotificationUiState.ActiveExercise).timer
        )

        val zeroTick =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.Running(Duration.ZERO),
                    progress = progress
                )
            ) as TrainingNotificationUiState.ActiveExercise
        assertEquals(NotificationTimer.Running(0L), zeroTick.timer)

        val overflowTick =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.OverflowRunning((-2300).milliseconds),
                    progress = progress
                )
            ) as TrainingNotificationUiState.ActiveExercise
        assertEquals(NotificationTimer.Overflow(2L), overflowTick.timer)

        val readyTick =
            mapper.map(
                TrainingSessionState.ExerciseState(
                    sessionExercise = exercise,
                    timerState = TimerState.Ready(45.seconds),
                    progress = progress
                )
            ) as TrainingNotificationUiState.ActiveExercise
        assertEquals(NotificationTimer.Ready(45L), readyTick.timer)
    }
}
