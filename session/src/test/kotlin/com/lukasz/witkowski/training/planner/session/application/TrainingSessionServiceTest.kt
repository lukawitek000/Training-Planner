package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.createTrainingExercise
import com.lukasz.witkowski.training.planner.session.createTrainingPlan
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionEvent
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SessionStatisticsRecorder
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionServiceTest : TrainingSessionTest() {
    private val timeProvider: FixedTimeProvider = FixedTimeProvider()
    private val trainingSetsPolicy: TrainingSetsPolicy = StraightSetsPolicy()
    private val recordedEvents = mutableListOf<SessionEvent>()
    private val completedStatistics = mutableListOf<TrainingStatistics>()
    private val updatedMetadata =
        mutableListOf<Triple<TrainingStatisticsId, SessionRpe?, String?>>()

    private val sessionCompletedListener =
        object : SessionCompletedListener {
            override suspend fun onSessionCompleted(statistics: TrainingStatistics) {
                completedStatistics.add(statistics)
            }

            override suspend fun onSessionMetadataUpdated(
                id: TrainingStatisticsId,
                rpe: SessionRpe?,
                note: String?
            ) {
                updatedMetadata.add(Triple(id, rpe, note))
            }
        }

    private lateinit var trainingExercises: List<TrainingExercise>
    private lateinit var trainingPlan: TrainingPlan
    private lateinit var trainingSessionService: TrainingSessionService

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setUp() {
        recordedEvents.clear()
        completedStatistics.clear()
        updatedMetadata.clear()
        trainingExercises = TRAINING_EXERCISES
        trainingPlan = TRAINING_PLAN
        trainingSessionService =
            TrainingSessionService(
                sessionCompletedListener = sessionCompletedListener,
                timeProvider = timeProvider,
                timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    tickDelay = 1.seconds,
                    timerDispatcher = testDispatcher
                ),
                trainingSetsStrategy = trainingSetsPolicy,
                backgroundDispatcher = UnconfinedTestDispatcher(scheduler),
                ioDispatcher = UnconfinedTestDispatcher(scheduler),
                sessionStatisticsRecorder = SessionStatisticsRecorder { recordedEvents.add(it) }
            )
    }

    @Test
    fun `initial state before starting session is IdleState and trainingPlan is null`() {
        assertEquals(
            TrainingSessionState.IdleState,
            trainingSessionService.trainingSessionState.value
        )
        assertNull(trainingSessionService.trainingPlan)
    }

    @Test
    fun `calling any action or command without starting session throws IllegalStateException`() {
        val actions: List<() -> Unit> =
            listOf(
                { trainingSessionService.skip() },
                { trainingSessionService.completed() },
                { trainingSessionService.changeReps(10) },
                { trainingSessionService.changeWeight(50.0) },
                { trainingSessionService.addExtraSet() },
                { trainingSessionService.removeExtraSet() },
                { trainingSessionService.setExerciseSetFeedback(SetEffort.MODERATE) },
                { trainingSessionService.updateHoldTime(30.seconds) },
                { trainingSessionService.startTimer() },
                { trainingSessionService.resetTimer() },
                { trainingSessionService.updateSessionNotes("note") },
                { trainingSessionService.updateSessionFeedback(SessionRpe.MODERATE) },
                { trainingSessionService.endEarly() },
                { trainingSessionService.handle(SessionCommand.Exercise.UpdateReps(12)) }
            )

        actions.forEach { action ->
            assertFailsWith<IllegalStateException> {
                action()
            }
        }
    }

    @Test
    fun `startTraining sets trainingPlan, creates active session, and emits first ExerciseState`() {
        // when
        trainingSessionService.startTraining(trainingPlan)

        // then
        assertEquals(trainingPlan, trainingSessionService.trainingPlan)
        assertExerciseState(
            expectedExerciseId = trainingExercises.first().id,
            trainingSessionState = trainingSessionService.trainingSessionState.value
        )
        assertTrue(recordedEvents.any { it is SessionEvent.SessionStarted })
        assertTrue(recordedEvents.any { it is SessionEvent.ExerciseStarted })
    }

    @Test
    fun `startTraining with a new plan replaces the active session state`() {
        // given
        val secondExercise = createTrainingExercise(name = "Second Plan Exercise", sets = 1)
        val secondPlan = createTrainingPlan(listOf(secondExercise))
        trainingSessionService.startTraining(trainingPlan)

        // when
        trainingSessionService.startTraining(secondPlan)

        // then
        assertEquals(secondPlan, trainingSessionService.trainingPlan)
        assertExerciseState(
            expectedExerciseId = secondExercise.id,
            trainingSessionState = trainingSessionService.trainingSessionState.value
        )
    }

    @Test
    fun `exercise action and command wrappers delegate to active TrainingSession`() {
        // given: isometric exercise with 1 set so timer and exercise commands can be verified
        val isometricExercise =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                repetitions = 10,
                sets = 1,
                restTime = 30.seconds,
                time = 20.seconds,
                exercise = createDummyExercise("Plank")
            )
        val plan = createTrainingPlan(listOf(isometricExercise))
        trainingSessionService.startTraining(plan)

        // when: delegate reps, weight, holdTime, timer start/reset, add/remove extra set, and handle
        trainingSessionService.changeReps(15)
        trainingSessionService.changeWeight(65.5)
        trainingSessionService.updateHoldTime(35.seconds)
        trainingSessionService.startTimer()
        val runningState =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.ExerciseState
        assertEquals(TimerState.Running(35.seconds), runningState.timerState)
        trainingSessionService.resetTimer()
        trainingSessionService.addExtraSet()
        val stateAfterAdd =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.ExerciseState
        assertEquals(2, stateAfterAdd.progress.currentExerciseTotalSets)
        trainingSessionService.removeExtraSet()
        trainingSessionService.handle(SessionCommand.Exercise.UpdateReps(18))

        // then
        val exerciseState =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.ExerciseState
        assertEquals(18, exerciseState.sessionExercise.currentConfig.reps)
        assertEquals(65.5, exerciseState.sessionExercise.currentConfig.weightInKg!!, 0.01)
        assertEquals(35.seconds, exerciseState.sessionExercise.currentConfig.holdTime)
        assertEquals(TimerState.Ready(35.seconds), exerciseState.timerState)
        assertEquals(1, exerciseState.progress.currentExerciseTotalSets)
    }

    @Test
    fun `rest, summary, complete, skip, and endEarly wrappers delegate to active session`() {
        // given: 1 exercise with 2 sets
        val exercise = createTrainingExercise(sets = 2, restTime = 30.seconds)
        val plan = createTrainingPlan(listOf(exercise))
        trainingSessionService.startTraining(plan)

        // when: complete Set 1 -> enters RestTimeState, set feedback, and skip rest -> Set 2
        trainingSessionService.completed()
        trainingSessionService.setExerciseSetFeedback(SetEffort.HARD)
        val restState =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.RestTimeState
        assertEquals(SetEffort.HARD, restState.selectedFeedback)

        trainingSessionService.skip() // skip rest -> Set 2
        trainingSessionService.completed() // complete Set 2 -> SummaryState

        // when: update summary notes and RPE
        trainingSessionService.updateSessionNotes("Felt great")
        trainingSessionService.updateSessionFeedback(SessionRpe.HARD)
        val summaryState =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.SummaryState
        assertEquals("Felt great", summaryState.notes)
        assertEquals(SessionRpe.HARD, summaryState.selectedOverallRpe)

        // when: start a new session and call endEarly
        trainingSessionService.startTraining(plan)
        trainingSessionService.endEarly()
        assertEquals(
            TrainingSessionState.IdleState,
            trainingSessionService.trainingSessionState.value
        )
    }

    @Test
    fun `reaching summary state eagerly triggers onSessionCompleted on sessionCompletedListener`() {
        // given
        val exercise = createTrainingExercise(sets = 1)
        val plan = createTrainingPlan(listOf(exercise))
        trainingSessionService.startTraining(plan)

        // when: complete Set 1 -> transitions to SummaryState
        trainingSessionService.completed()

        // then
        assertTrue(
            trainingSessionService.trainingSessionState.value is TrainingSessionState.SummaryState
        )
        assertEquals(1, completedStatistics.size)
        assertEquals(plan.id, completedStatistics.first().trainingPlanId)
        assertTrue(updatedMetadata.isEmpty())
    }

    @Test
    fun `finishWorkout updates metadata and resets active session state to IdleState`() {
        // given
        val exercise = createTrainingExercise(sets = 1)
        val plan = createTrainingPlan(listOf(exercise))
        trainingSessionService.startTraining(plan)
        trainingSessionService.completed() // reach summary
        val summaryState =
            trainingSessionService.trainingSessionState.value as TrainingSessionState.SummaryState
        val statsId = summaryState.statistics.id

        // when
        trainingSessionService.updateSessionFeedback(SessionRpe.VERY_HARD)
        trainingSessionService.updateSessionNotes("Challenging workout")
        trainingSessionService.finishWorkout()

        // then
        assertEquals(1, completedStatistics.size)
        assertEquals(1, updatedMetadata.size)
        val (updatedId, updatedRpe, updatedNote) = updatedMetadata.first()
        assertEquals(statsId, updatedId)
        assertEquals(SessionRpe.VERY_HARD, updatedRpe)
        assertEquals("Challenging workout", updatedNote)
        assertEquals(
            TrainingSessionState.IdleState,
            trainingSessionService.trainingSessionState.value
        )
        assertNull(trainingSessionService.trainingPlan)
    }
}
