package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.TRAINING_EXERCISES
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.TrainingSessionTest
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.SessionCommand
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.statisticsrecorder.FixedTimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingSessionStatisticsTest : TrainingSessionTest() {
    private val trainingExercises: List<TrainingExercise> = TRAINING_EXERCISES
    private val trainingPlan: TrainingPlan = TRAINING_PLAN

    @Test
    fun `record single exercise attempt with completed status`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val timeProvider = FixedTimeProvider()
            val trainingSession =
                createTrainingSession(
                    trainingPlan = trainingPlan,
                    scope = backgroundScope,
                    timeProvider = timeProvider
                )
            val exerciseToRecordAttempt = trainingExercises.first()

            // when
            trainingSession.start()
            timeProvider.advanceBy(TIME_10_SECONDS)
            trainingSession.complete()
            val trainingStatistics =
                completeRestOfTheTraining(trainingSession, timeProvider).statistics
            val attemptStatistics =
                trainingStatistics.exercisesStatistics
                    .first()
                    .attemptsStatistics
                    .first()

            // then
            val expectedStatistics =
                ExerciseAttemptStatistics(
                    trainingExerciseId = exerciseToRecordAttempt.id,
                    time = TIME_10_SECONDS,
                    set = 1,
                    completed = true,
                    actualReps = exerciseToRecordAttempt.repetitions,
                    actualWeightInKg = exerciseToRecordAttempt.weightInKg?.toDouble()
                )
            assertAttemptStatistics(expectedStatistics, attemptStatistics)
        }

    @Test
    @Suppress("LongMethod")
    fun `record statistics for exercise from 3 attempts`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val timeProvider = FixedTimeProvider()
        val trainingSession =
            createTrainingSession(
                trainingPlan = trainingPlan,
                scope = backgroundScope,
                timeProvider = timeProvider
            )
        val exerciseToRecordStatistics = trainingExercises[1]

        // when
        trainingSession.start()
        val summaryState = completeRestOfTheTraining(trainingSession, timeProvider)

        val trainingStatistics = summaryState.statistics
        val exerciseStatistics = trainingStatistics.exercisesStatistics[1]

        // then
        val expectedAttemptsStatistics =
            listOf(
                ExerciseAttemptStatistics(
                    trainingExerciseId = exerciseToRecordStatistics.id,
                    time = TIME_10_SECONDS,
                    set = 1,
                    completed = true,
                    actualReps = exerciseToRecordStatistics.repetitions,
                    actualWeightInKg = exerciseToRecordStatistics.weightInKg?.toDouble()
                ),
                ExerciseAttemptStatistics(
                    trainingExerciseId = exerciseToRecordStatistics.id,
                    time = TIME_10_SECONDS,
                    set = 2,
                    completed = true,
                    actualReps = exerciseToRecordStatistics.repetitions,
                    actualWeightInKg = exerciseToRecordStatistics.weightInKg?.toDouble()
                ),
                ExerciseAttemptStatistics(
                    trainingExerciseId = exerciseToRecordStatistics.id,
                    time = TIME_10_SECONDS,
                    set = 3,
                    completed = true,
                    actualReps = exerciseToRecordStatistics.repetitions,
                    actualWeightInKg = exerciseToRecordStatistics.weightInKg?.toDouble()
                )
            )
        val expectedStatistics =
            ExerciseStatistics(
                trainingExerciseId = exerciseToRecordStatistics.id,
                attemptsStatistics = expectedAttemptsStatistics
            )
        assertExerciseStatistics(expectedStatistics, exerciseStatistics)
    }

    @Test
    fun `record statistics for completed training`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val timeProvider = FixedTimeProvider()
        val trainingSession =
            createTrainingSession(
                trainingPlan = trainingPlan,
                scope = backgroundScope,
                timeProvider = timeProvider
            )

        // when
        trainingSession.start()
        val summaryState = completeRestOfTheTraining(trainingSession, timeProvider)
        val statistics = summaryState.statistics

        // then
        val expectedStatistics = createExpectedStatisticsForCompletedTraining()
        assertTrainingStatistics(expectedStatistics, statistics)
    }

    @Test
    fun `summary phase updates overall RPE and notes without mutating statistics directly`() =
        runTest(UnconfinedTestDispatcher()) {
            // given
            val timeProvider = FixedTimeProvider()
            val trainingSession =
                createTrainingSession(
                    trainingPlan = trainingPlan,
                    scope = backgroundScope,
                    timeProvider = timeProvider
                )
            trainingSession.start()
            completeRestOfTheTraining(trainingSession, timeProvider)

            // when
            trainingSession.handle(SessionCommand.Summary.UpdateOverallRpe(SessionRpe.NEAR_LIMIT))
            trainingSession.handle(SessionCommand.Summary.UpdateNotes("Felt strong"))

            // then: SummaryState holds selectedOverallRpe & notes, while statistics remains unmutated
            val summaryState = trainingSession.state.value as TrainingSessionState.SummaryState
            assertEquals(SessionRpe.NEAR_LIMIT, summaryState.selectedOverallRpe)
            assertEquals("Felt strong", summaryState.notes)
            assertNull(summaryState.statistics.rpe)
            assertNull(summaryState.statistics.note)
        }

    @Suppress("LongMethod")
    private fun createExpectedStatisticsForCompletedTraining() = TrainingStatistics(
        trainingPlanId = trainingPlan.id,
        totalTime = Time(seconds = 190),
        date = Date(0L),
        exercisesStatistics =
        listOf(
            ExerciseStatistics(
                trainingExerciseId = trainingExercises.first().id,
                attemptsStatistics =
                listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises.first().id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true,
                        actualReps = trainingExercises.first().repetitions,
                        actualWeightInKg =
                        trainingExercises.first().weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises.first().id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true,
                        actualReps = trainingExercises.first().repetitions,
                        actualWeightInKg =
                        trainingExercises.first().weightInKg?.toDouble()
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[1].id,
                attemptsStatistics =
                listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true,
                        actualReps = trainingExercises[1].repetitions,
                        actualWeightInKg = trainingExercises[1].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true,
                        actualReps = trainingExercises[1].repetitions,
                        actualWeightInKg = trainingExercises[1].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[1].id,
                        time = TIME_10_SECONDS,
                        set = 3,
                        completed = true,
                        actualReps = trainingExercises[1].repetitions,
                        actualWeightInKg = trainingExercises[1].weightInKg?.toDouble()
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[2].id,
                attemptsStatistics =
                listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[2].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true,
                        actualReps = trainingExercises[2].repetitions,
                        actualWeightInKg = trainingExercises[2].weightInKg?.toDouble()
                    )
                )
            ),
            ExerciseStatistics(
                trainingExerciseId = trainingExercises[3].id,
                attemptsStatistics =
                listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 1,
                        completed = true,
                        actualReps = trainingExercises[3].repetitions,
                        actualWeightInKg = trainingExercises[3].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 2,
                        completed = true,
                        actualReps = trainingExercises[3].repetitions,
                        actualWeightInKg = trainingExercises[3].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 3,
                        completed = true,
                        actualReps = trainingExercises[3].repetitions,
                        actualWeightInKg = trainingExercises[3].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 4,
                        completed = true,
                        actualReps = trainingExercises[3].repetitions,
                        actualWeightInKg = trainingExercises[3].weightInKg?.toDouble()
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = trainingExercises[3].id,
                        time = TIME_10_SECONDS,
                        set = 5,
                        completed = true,
                        actualReps = trainingExercises[3].repetitions,
                        actualWeightInKg = trainingExercises[3].weightInKg?.toDouble()
                    )
                )
            )
        )
    )

    private fun completeRestOfTheTraining(
        trainingSession: TrainingSession,
        timeProvider: FixedTimeProvider
    ): TrainingSessionState.SummaryState {
        do {
            timeProvider.advanceBy(TIME_10_SECONDS)
            trainingSession.complete()
        } while (trainingSession.state.value !is TrainingSessionState.SummaryState)
        return trainingSession.state.value as TrainingSessionState.SummaryState
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
            exerciseStatistics.completenessRate,
            0.001
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
