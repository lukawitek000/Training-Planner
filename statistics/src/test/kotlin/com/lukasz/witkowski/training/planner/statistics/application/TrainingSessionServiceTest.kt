package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.statistics.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.statistics.TrainingSessionTest
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.timer.Timer
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// TODO use new timer in the wearable
// remove old timer controller
@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionServiceTest : TrainingSessionTest() {

    private val timeProvider: FixedTimeProvider = FixedTimeProvider()
    private val trainingSetsPolicy: TrainingSetsPolicy = CircuitSetsPolicy()

    private lateinit var trainingExercises: List<TrainingExercise>
    private lateinit var trainingPlan: TrainingPlan
    private lateinit var trainingSessionService: TrainingSessionService

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setUp() {
        trainingExercises = TRAINING_EXERCISES
        trainingPlan = TRAINING_PLAN
        trainingSessionService = TrainingSessionService(
            timeProvider,
            Timer(timerDispatcher = testDispatcher),
            trainingSetsPolicy,
            testDispatcher
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
    fun `emit 1st exercise, rest time and 2nd exercise`() = runTest(UnconfinedTestDispatcher()) {
        val testResult = mutableListOf<TrainingSessionState>()
        val job = launch {
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
            expectedState = TrainingSessionState.RestTimeState(
                secondExercise,
                firstExercise.restTime
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
    fun `time is configured to the first exercise time`() {
        whenStartTrainingSession()

        assertEquals(trainingExercises.first().time, trainingSessionService.time.value)
    }

    @Test
    fun `exercise state is the same after the timer has finished`() = runTest(testDispatcher) {
        whenStartTrainingSession()
        trainingSessionService.startTimer()

        scheduler.advanceTimeBy(trainingExercises.first().time.timeInMillis + 1)

        assert(!trainingSessionService.isTimerRunning.value)
        assertExerciseState(
            expectedState = TrainingSessionState.ExerciseState(trainingExercises.first()),
            trainingSessionState = trainingSessionService.trainingSessionState.value
        )

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

            assertRestTimeState(
                TrainingSessionState.RestTimeState(
                    trainingExercises[1],
                    trainingExercises.first().restTime
                ), trainingSessionService.trainingSessionState.value
            )

            assert(trainingSessionService.isTimerRunning.value)
            scheduler.advanceTimeBy(trainingExercises.first().restTime.timeInMillis + 1)

            assert(!trainingSessionService.isTimerRunning.value)

            assertExerciseState(
                expectedState = TrainingSessionState.ExerciseState(trainingExercises[1]),
                trainingSessionState = trainingSessionService.trainingSessionState.value
            )
        }

    private fun whenStartTrainingSession() {
        trainingSessionService.startTraining(trainingPlan)
    }
}