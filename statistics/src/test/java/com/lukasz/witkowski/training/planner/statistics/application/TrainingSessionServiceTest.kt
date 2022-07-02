package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.statistics.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.statistics.TrainingSessionTest
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import kotlin.test.assertFailsWith

internal class TrainingSessionServiceTest : TrainingSessionTest() {

    private val timeProvider: TimeProvider = FixedTimeProvider()
    private val trainingSetsPolicy: TrainingSetsPolicy = CircuitSetsPolicy()

    private lateinit var trainingExercises: List<TrainingExercise>
    private lateinit var trainingPlan: TrainingPlan
    private lateinit var trainingSessionService: TrainingSessionService

    @Before
    fun setUp() {
        trainingExercises = TRAINING_EXERCISES
        trainingPlan = TRAINING_PLAN
        trainingSessionService = TrainingSessionService(timeProvider, trainingSetsPolicy)
    }

    @Test
    fun `load first exercise after training starts`() {
        val state = whenStartTrainingSession()

        assertExerciseState(
            expectedState = TrainingSessionState.ExerciseState(trainingExercises.first()),
            trainingSessionState = state
        )
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
    fun `call completed method after session was stopped`() {
        whenStartTrainingSession()
        trainingSessionService.stopTraining()

        assertFailsWith<Exception> {
            trainingSessionService.completed()
        }
    }

    private fun whenStartTrainingSession() = trainingSessionService.startTraining(trainingPlan)
}