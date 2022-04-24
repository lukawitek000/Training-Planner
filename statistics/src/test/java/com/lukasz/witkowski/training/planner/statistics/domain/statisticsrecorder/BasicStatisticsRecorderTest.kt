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
import java.util.Date
import kotlin.math.exp
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
        val mockedElapsedTime = Time(1000L)
        timeProvider.time = mockedElapsedTime
        val trainingStatistics = basicStatisticsRecorder.stop()

        // then
        val expectedStatistics = TrainingStatistics(trainingPlanId = trainingPlan.id, totalTime = mockedElapsedTime, date = Date(), exercisesStatistics = emptyList())
        assertTrainingStatistics(expectedStatistics, trainingStatistics)

    }

    private fun assertTrainingStatistics(
        expectedStatistics: TrainingStatistics,
        trainingStatistics: TrainingStatistics
    ) {
        assertEquals(expectedStatistics.trainingPlanId, trainingStatistics.trainingPlanId)
        assertEquals(expectedStatistics.totalTime, trainingStatistics.totalTime)
        assertExercisesStatistics(expectedStatistics.exercisesStatistics, trainingStatistics.exercisesStatistics)
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
        assertEquals(expectedExerciseStatistics.trainingExerciseId, exerciseStatistics.trainingExerciseId)
        assertEquals(expectedExerciseStatistics.totalTime, exerciseStatistics.totalTime)
        assertEquals(expectedExerciseStatistics.completenessRate, exerciseStatistics.completenessRate)
        assertAttemptsStatistics(expectedExerciseStatistics.attemptsStatistics, exerciseStatistics.attemptsStatistics)
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

    private fun assertAttemptStatistics(expectedAttemptStatistics: ExerciseAttemptStatistics, attemptStatistics: ExerciseAttemptStatistics) {
        assertEquals(expectedAttemptStatistics.trainingExerciseId, attemptStatistics.trainingExerciseId)
        assertEquals(expectedAttemptStatistics.completed, attemptStatistics.completed)
        assertEquals(expectedAttemptStatistics.time, attemptStatistics.time)
        assertEquals(expectedAttemptStatistics.set, attemptStatistics.set)
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
            sets = 2
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
}