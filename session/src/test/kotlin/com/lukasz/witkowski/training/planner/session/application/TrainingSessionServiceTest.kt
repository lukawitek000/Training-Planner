package com.lukasz.witkowski.training.planner.session.application

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.domain.SessionCompletedListener
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerFactory
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
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
    private val trainingSetsPolicy: TrainingSetsPolicy = StraightSetsPolicy()

    private lateinit var trainingExercises: List<TrainingExercise>
    private lateinit var trainingPlan: TrainingPlan
    private lateinit var trainingSessionService: TrainingSessionService

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setUp() {
        trainingExercises = TRAINING_EXERCISES
        trainingPlan = TRAINING_PLAN
        trainingSessionService =
            TrainingSessionService(
                timeProvider = timeProvider,
                timerFactory =
                TimerFactory(
                    timeProvider = timeProvider,
                    tickDelay = 1.seconds,
                    timerDispatcher = testDispatcher
                ),
                trainingSetsStrategy = trainingSetsPolicy,
                backgroundDispatcher = UnconfinedTestDispatcher(scheduler)
            )
    }

    @Test
    fun `call skip method without starting training session`() {
        assertFailsWith<IllegalStateException> {
            trainingSessionService.skip()
        }
    }
}
