package com.lukasz.witkowski.training.planner.analytics.infrastructure

import android.content.Context
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DbStatisticsRepositoryTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }
    private lateinit var database: StatisticsDatabase
    private lateinit var repository: DbStatisticsRepository

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    context = context,
                    klass = StatisticsDatabase::class.java
                ).allowMainThreadQueries()
                .build()
        repository = DbStatisticsRepository(statisticsDao = database.statisticsDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveAndRetrieveTrainingStatisticsWithV2RichTelemetry() = runTest {
        val trainingPlanId = TrainingPlanId.create()
        val trainingExerciseId = TrainingExerciseId.create()
        val statisticsId = TrainingStatisticsId.create()
        val exerciseStatisticsId = ExerciseStatisticsId.create()
        val attemptId = ExerciseAttemptStatisticsId.create()

        val attempt =
            ExerciseAttemptStatistics(
                id = attemptId,
                trainingExerciseId = trainingExerciseId,
                time = Time(30_000L),
                set = 1,
                completed = true,
                actualReps = 12,
                actualWeightInKg = 85.5,
                rpe = 8.5f
            )

        val exerciseStatistics =
            ExerciseStatistics(
                id = exerciseStatisticsId,
                trainingExerciseId = trainingExerciseId,
                attemptsStatistics = listOf(attempt)
            )

        val statistics =
            TrainingStatistics(
                id = statisticsId,
                trainingPlanId = trainingPlanId,
                totalTime = Time(120_000L),
                date = Date(1_000_000L),
                exercisesStatistics = listOf(exerciseStatistics),
                note = "Strong session, felt good on bench press",
                rpe = 8
            )

        repository.save(statistics)

        val retrieved = repository.getByTrainingStatisticsId(statisticsId).first()
        assertNotNull(retrieved)
        assertEquals(statisticsId, retrieved.id)
        assertEquals(trainingPlanId, retrieved.trainingPlanId)
        assertEquals("Strong session, felt good on bench press", retrieved.note)
        assertEquals(8, retrieved.rpe)
        assertEquals(1, retrieved.exercisesStatistics.size)

        val retrievedExercise = retrieved.exercisesStatistics.first()
        assertEquals(trainingExerciseId, retrievedExercise.trainingExerciseId)
        assertEquals(1, retrievedExercise.attemptsStatistics.size)

        val retrievedAttempt = retrievedExercise.attemptsStatistics.first()
        assertEquals(attemptId, retrievedAttempt.id)
        assertEquals(12, retrievedAttempt.actualReps)
        assertEquals(85.5, retrievedAttempt.actualWeightInKg)
        assertEquals(8.5f, retrievedAttempt.rpe)
        assertEquals(true, retrievedAttempt.completed)
    }

    @Test
    fun getTrainingStatisticsByTrainingPlanIdReturnsAllMatchingStatistics() = runTest {
        val planId = TrainingPlanId.create()
        val otherPlanId = TrainingPlanId.create()

        val stats1 =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = planId,
                totalTime = Time(60_000L),
                date = Date(100L),
                exercisesStatistics = emptyList(),
                note = "Plan 1 Session 1"
            )
        val stats2 =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = planId,
                totalTime = Time(90_000L),
                date = Date(200L),
                exercisesStatistics = emptyList(),
                note = "Plan 1 Session 2"
            )
        val otherStats =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = otherPlanId,
                totalTime = Time(45_000L),
                date = Date(300L),
                exercisesStatistics = emptyList(),
                note = "Other Plan Session"
            )

        repository.save(stats1)
        repository.save(stats2)
        repository.save(otherStats)

        val planStats = repository.getByTrainingPlanId(planId).first()
        assertEquals(2, planStats.size)
        assertEquals(
            setOf("Plan 1 Session 1", "Plan 1 Session 2"),
            planStats.map {
                it.note
            }.toSet()
        )
    }
}
