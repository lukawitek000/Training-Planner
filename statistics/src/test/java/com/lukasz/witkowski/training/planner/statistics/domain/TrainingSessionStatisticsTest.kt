package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.statistics.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals

internal class TrainingSessionStatisticsTest : TrainingSessionTest() {

    private val trainingExercises: List<TrainingExercise> = TRAINING_EXERCISES
    private val trainingPlan: TrainingPlan = TRAINING_PLAN
    private val trainingSetsPolicy: TrainingSetsPolicy = CircuitSetsPolicy()
    private lateinit var trainingSession: TrainingSession

    @Before
    fun setUp() {
        trainingSession = TrainingSession(trainingPlan, trainingSetsPolicy)
    }

    @Test
    fun `record single exercise attempt with completed status`() {
        // given
        val exerciseToRecordAttempt = trainingExercises.first()

        // when
        trainingSession.start(Time.NONE)
        trainingSession.completed(TIME_10_SECONDS)
        val trainingStatistics = completeRestOfTheTraining(trainingSession).statistics
        val attemptStatistics =
            trainingStatistics.exercisesStatistics.first().attemptsStatistics.first()

        // then
        val expectedStatistics = ExerciseAttemptStatistics(
            trainingExerciseId = exerciseToRecordAttempt.id,
            time = TIME_10_SECONDS,
            set = 1,
            completed = true
        )
        assertAttemptStatistics(expectedStatistics, attemptStatistics)
    }

    @Test
    fun `record statistics for exercise from 3 attempts`() {
        // given
        val exerciseToRecordStatistics = trainingExercises[1]

        // when
        // when
        trainingSession.start(Time.NONE)
        val summaryState = completeRestOfTheTraining(trainingSession)

        val trainingStatistics = summaryState.statistics
        val exerciseStatistics = trainingStatistics.exercisesStatistics[1]

        // then
        val expectedAttemptsStatistics = listOf(
            ExerciseAttemptStatistics(
                trainingExerciseId = exerciseToRecordStatistics.id,
                time = TIME_10_SECONDS,
                set = 1,
                completed = true
            ),
            ExerciseAttemptStatistics(
                trainingExerciseId = exerciseToRecordStatistics.id,
                time = TIME_10_SECONDS,
                set = 2,
                completed = true
            ),
            ExerciseAttemptStatistics(
                trainingExerciseId = exerciseToRecordStatistics.id,
                time = TIME_10_SECONDS,
                set = 3,
                completed = true
            ),
        )
        val expectedStatistics = ExerciseStatistics(
            trainingExerciseId = exerciseToRecordStatistics.id,
            attemptsStatistics = expectedAttemptsStatistics
        )
        assertExerciseStatistics(expectedStatistics, exerciseStatistics)
    }

    @Test
    fun `record statistics for completed training`() {
        // when
        trainingSession.start(Time.NONE)
        val summaryState = completeRestOfTheTraining(trainingSession)
        val statistics = summaryState.statistics

        // then
        val expectedStatistics = createExpectedStatisticsForCompletedTraining()
        assertTrainingStatistics(expectedStatistics, statistics)
    }

    private fun createExpectedStatisticsForCompletedTraining() = TrainingStatistics(
        trainingPlanId = trainingPlan.id,
        totalTime = Time(seconds = 180),
        date = Date(),
        exercisesStatistics = listOf(
            ExerciseStatistics(
                trainingExerciseId = trainingExercises.first().id,
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises.first().id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises.first().id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[1].id,
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 3,
                        completed = true
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[2].id,
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[2].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[3].id,
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 3,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 4,
                        completed = true
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 5,
                        completed = true
                    )
                )
            )
        )
    )

    private fun completeRestOfTheTraining(
        trainingSession: TrainingSession,
        startTime: Time = Time.NONE
    ): TrainingSessionState.SummaryState {
        var time = startTime
        var state: TrainingSessionState? = null
        do {
            time += TIME_10_SECONDS
            state = trainingSession.completed(time)
        } while (state !is TrainingSessionState.SummaryState)
        return state
    }

    private fun assertTrainingStatistics(
        expectedStatistics: TrainingStatistics,
        trainingStatistics: TrainingStatistics
    ) {
        assertEquals(expectedStatistics.trainingPlanId, trainingStatistics.trainingPlanId)
        assertEqualsTime(expectedStatistics.totalTime, trainingStatistics.totalTime)
        assertExercisesStatistics(
            expectedStatistics.exercisesStatistics,
            trainingStatistics.exercisesStatistics
        )
    }

    private fun assertExercisesStatistics(
        expectedExercisesStatistics: List<ExerciseStatistics>,
        exercisesStatistics: List<ExerciseStatistics>
    ) {
        assertEquals(expectedExercisesStatistics.size, exercisesStatistics.size)
        for (i in exercisesStatistics.indices) {
            assertExerciseStatistics(expectedExercisesStatistics[i], exercisesStatistics[i])
        }
    }

    private fun assertExerciseStatistics(
        expectedExerciseStatistics: ExerciseStatistics,
        exerciseStatistics: ExerciseStatistics
    ) {
        assertEquals(
            expectedExerciseStatistics.trainingExerciseId,
            exerciseStatistics.trainingExerciseId
        )
        assertEqualsTime(expectedExerciseStatistics.totalTime, exerciseStatistics.totalTime)
        assertEquals(
            expectedExerciseStatistics.completenessRate,
            exerciseStatistics.completenessRate
        )
        assertAttemptsStatistics(
            expectedExerciseStatistics.attemptsStatistics,
            exerciseStatistics.attemptsStatistics
        )
    }

    private fun assertAttemptsStatistics(
        expectedAttemptsStatistics: List<ExerciseAttemptStatistics>,
        attemptsStatistics: List<ExerciseAttemptStatistics>
    ) {
        assertEquals(expectedAttemptsStatistics.size, attemptsStatistics.size)
        for (i in attemptsStatistics.indices) {
            assertAttemptStatistics(expectedAttemptsStatistics[i], attemptsStatistics[i])
        }
    }

    private fun assertAttemptStatistics(
        expectedAttemptStatistics: ExerciseAttemptStatistics,
        attemptStatistics: ExerciseAttemptStatistics
    ) {
        assertEquals(
            expectedAttemptStatistics.trainingExerciseId,
            attemptStatistics.trainingExerciseId
        )
        assertEquals(expectedAttemptStatistics.completed, attemptStatistics.completed)
        assertEqualsTime(expectedAttemptStatistics.time, attemptStatistics.time)
        assertEquals(expectedAttemptStatistics.set, attemptStatistics.set)
    }

    private fun assertEqualsTime(expected: Time, actual: Time) {
        assertEquals(expected.timeInMillis, actual.timeInMillis)
    }

    private companion object {
        val TIME_10_SECONDS = Time(10000L)
    }
}