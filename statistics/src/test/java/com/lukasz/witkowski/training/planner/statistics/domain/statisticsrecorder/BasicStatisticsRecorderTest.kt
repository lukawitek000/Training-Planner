package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BasicStatisticsRecorderTest {

    private val trainingExercises: List<TrainingExercise> = createTrainingExercises()
    private val trainingPlan: TrainingPlan = createTrainingPlan(trainingExercises)
    private val timeProvider = FixedTimeProvider()
    private lateinit var basicStatisticsRecorder: BasicStatisticsRecorder

    @Before
    fun setUp() {
        basicStatisticsRecorder = BasicStatisticsRecorder(trainingPlan.id, timeProvider)
    }

    @Test
    fun `start stop recording statistics without exercises`() {
        // given

        // when
        basicStatisticsRecorder.start()
        timeHasPassedBy(TIME_10_SECONDS)
        val trainingStatistics = basicStatisticsRecorder.stop()

        // then
        val expectedStatistics = TrainingStatistics(
            trainingPlanId = trainingPlan.id,
            totalTime = TIME_10_SECONDS,
            date = timeProvider.date,
            exercisesStatistics = emptyList()
        )
        assertTrainingStatistics(expectedStatistics, trainingStatistics)
    }

    @Test
    fun `record single exercise attempt with completed status`() {
        // given
        val exerciseToRecordAttempt = trainingExercises.first()

        // when
        basicStatisticsRecorder.start()
        basicStatisticsRecorder.startRecordingExercise(exerciseToRecordAttempt.id, 1)
        timeHasPassedBy(TIME_10_SECONDS)
        basicStatisticsRecorder.stopRecordingExercise(true)
        val trainingStatistics = basicStatisticsRecorder.stop()
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
        val exerciseToRecordStatistics = trainingExercises.first()

        // when
        basicStatisticsRecorder.start()
        for (set in 1..exerciseToRecordStatistics.sets) {
            timeHasPassedBy(Time.NONE)
            basicStatisticsRecorder.startRecordingExercise(exerciseToRecordStatistics.id, set)
            timeHasPassedBy(TIME_10_SECONDS)
            basicStatisticsRecorder.stopRecordingExercise(set % 2 == 0)
        }
        val trainingStatistics = basicStatisticsRecorder.stop()
        val exerciseStatistics = trainingStatistics.exercisesStatistics.first()

        // then
        val expectedAttemptsStatistics = listOf(
            ExerciseAttemptStatistics(
                trainingExerciseId = exerciseToRecordStatistics.id,
                time = TIME_10_SECONDS,
                set = 1,
                completed = false
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
                completed = false
            ),
        )
        val expectedStatistics = ExerciseStatistics(
            trainingExerciseId = exerciseToRecordStatistics.id,
            attemptsStatistics = expectedAttemptsStatistics
        )
        assertExerciseStatistics(expectedStatistics, exerciseStatistics)
    }

    private fun timeHasPassedBy(mockedElapsedTime: Time) {
        timeProvider.time = mockedElapsedTime
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

    private fun createTrainingPlan(exercises: List<TrainingExercise>): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Test Training Plan",
            exercises = exercises
        )
    }

    private fun createTrainingExercises(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 3
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 3
        )
        val exercise3 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 1
        )
        val exercise4 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 5
        )
        return listOf(exercise1, exercise2, exercise3, exercise4)
    }

    private companion object {
        val TIME_10_SECONDS = Time(10000L)
    }
}