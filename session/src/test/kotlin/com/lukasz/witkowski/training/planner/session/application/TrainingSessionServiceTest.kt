package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.Timer
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionServiceTest : TrainingSessionTest() {
    private val timeProvider: FixedTimeProvider = FixedTimeProvider()
    private val trainingSetsPolicy: TrainingSetsPolicy = CircuitSetsPolicy()

    private lateinit var trainingExercises: List<TrainingExercise>
    private lateinit var trainingPlan: TrainingPlan
    private lateinit var trainingSessionService: TrainingSessionService

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    private val sessionCompletedListener: SessionCompletedListener = mockk(relaxed = true)

    @Before
    fun setUp() {
        trainingExercises = TRAINING_EXERCISES
        trainingPlan = TRAINING_PLAN
        trainingSessionService =
            TrainingSessionService(
                timeProvider = timeProvider,
                timer = Timer(timerDispatcher = testDispatcher),
                sessionCompletedListener = sessionCompletedListener,
                trainingSetsStrategy = trainingSetsPolicy,
                backgroundDispatcher = testDispatcher
            )
    }

    @Test
    fun `load first exercise after training starts`() = runTest {
        whenStartTrainingSession()
        val state = trainingSessionService.trainingSessionState.first()

        assertExerciseState(
            expectedState = TrainingSessionState.ExerciseState(trainingExercises.first()),
            trainingSessionState = state
        )
    }

    @Test
    @Suppress("LongMethod")
    fun `emit 1st exercise, rest time and 2nd exercise`() = runTest(UnconfinedTestDispatcher()) {
        val testResult = mutableListOf<TrainingSessionState>()
        val job =
            launch {
                trainingSessionService.trainingSessionState.toList(testResult)
            }
        whenStartTrainingSession()
        // complete 1st exercise
        trainingSessionService.completed()
        // skip rest time
        trainingSessionService.skip()

        val firstExercise = trainingExercises.first()
        assert(testResult.first() is TrainingSessionState.IdleState)
        assertExerciseState(
            expectedState = TrainingSessionState.ExerciseState(trainingExercises.first()),
            trainingSessionState = testResult[1]
        )
        val secondExercise = trainingExercises[1]
        assertRestTimeState(
            expectedState =
            TrainingSessionState.RestTimeState(
                secondExercise,
                Time(firstExercise.restTime.inWholeMilliseconds)
            ),
            trainingSessionState = testResult[2]
        )
        assertExerciseState(
            expectedState = TrainingSessionState.ExerciseState(secondExercise),
            trainingSessionState = testResult[3]
        )
        job.cancel()
    }

    @Test
    fun `call skip method without starting training session`() {
        assertFailsWith<UninitializedPropertyAccessException> {
            trainingSessionService.skip()
        }
    }

    @Test
    fun `call skip method after session was stopped`() {
        whenStartTrainingSession()
        trainingSessionService.stopTraining()

        assertFailsWith<Exception> {
            trainingSessionService.skip()
        }
    }

    @Test
    fun `timer is running after starting it`() {
        whenStartTrainingSession()
        trainingSessionService.startTimer()

        assert(trainingSessionService.isTimerRunning.value)
    }

    @Test
    fun `timer is not running after starting and stopping it`() {
        whenStartTrainingSession()
        trainingSessionService.startTimer()
        trainingSessionService.stopTimer()

        assert(!trainingSessionService.isTimerRunning.value)
    }

    @Test
    fun `training session is started after start called`() {
        whenStartTrainingSession()

        val isStarted = trainingSessionService.isTrainingSessionStarted()
        assert(isStarted)
    }

    @Test
    fun `call completed method after session was stopped`() {
        whenStartTrainingSession()
        trainingSessionService.stopTraining()

        assertFailsWith<Exception> {
            trainingSessionService.completed()
        }
    }

    @Test
    fun `timer is started after changing to rest time state`() {
        whenStartTrainingSession()
        trainingSessionService.completed()

        assert(trainingSessionService.isTimerRunning.value)
    }

    @Test
    fun `rest state changes to exercise state after the timer has finished`() =
        runTest(testDispatcher) {
            whenStartTrainingSession()
            trainingSessionService.completed()

            val firstExerciseRestMillis = trainingExercises.first().restTime.inWholeMilliseconds
            assertRestTimeState(
                TrainingSessionState.RestTimeState(
                    trainingExercises[1],
                    Time(firstExerciseRestMillis)
                ),
                trainingSessionService.trainingSessionState.value
            )

            assert(trainingSessionService.isTimerRunning.value)
            scheduler.advanceTimeBy(firstExerciseRestMillis + 1)

            assertExerciseState(
                expectedState = TrainingSessionState.ExerciseState(trainingExercises[1]),
                trainingSessionState = trainingSessionService.trainingSessionState.value
            )
        }

    @Test
    fun `throw when two same SessionFinishedListeners are registered`() {
        val listener = mockk<SessionFinishedListener>()
        trainingSessionService.addSessionFinishedListener(listener)

        assertFailsWith<IllegalArgumentException> {
            trainingSessionService.addSessionFinishedListener(listener)
        }
    }

    @Test
    fun `do not throw if same SessionFinishedListener was added after removing it`() {
        val listener = mockk<SessionFinishedListener>()
        trainingSessionService.addSessionFinishedListener(listener)
        trainingSessionService.removeSessionFinishedListener(listener)

        trainingSessionService.addSessionFinishedListener(listener)
    }

    @Test
    fun `inform SessionFinishedListener when session is finished`() = runTest(testDispatcher) {
        val listener = mockk<SessionFinishedListener>(relaxed = true)
        trainingSessionService.addSessionFinishedListener(listener)

        coEvery { sessionCompletedListener.onSessionCompleted(any()) } returns Unit

        whenStartTrainingSession()
        whenAllTrainingExercisesCompleted()
        advanceUntilIdle()

        val state = trainingSessionService.trainingSessionState.value
        assertTrue(state is TrainingSessionState.SummaryState)
        val summaryState = state as TrainingSessionState.SummaryState

        trainingSessionService.finishSession(note = "Great workout", rpe = 8)

        verify(exactly = 1) { listener.onSessionFinished(summaryState.statistics.id) }
        coVerify(exactly = 1) {
            sessionCompletedListener.onSessionCompleted(
                match { it.note == "Great workout" && it.rpe == 8 }
            )
        }
    }

    private fun whenStartTrainingSession() {
        trainingSessionService.startTraining(trainingPlan)
    }

    private fun whenAllTrainingExercisesCompleted() {
        val steps =
            TRAINING_EXERCISES
                .fold(0) { acc, trainingExercise ->
                    val restTime = if (trainingExercise.hasRestTime()) 1 else 0
                    acc + (restTime + 1) * trainingExercise.sets
                }.let {
                    if (TRAINING_EXERCISES.last().hasRestTime()) it - 1 else it
                }
        for (i in 0 until steps) {
            trainingSessionService.completed()
        }
    }

    private fun TrainingExercise.hasRestTime() = restTime.isPositive()
}
