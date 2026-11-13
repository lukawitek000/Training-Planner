package com.lukasz.witkowski.training.planner.analytics.infrastructure

import android.content.Context
import androidx.paging.testing.asSnapshot
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.StatisticsDatabase
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
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
                time = 30.seconds,
                set = 1,
                completed = true,
                actualReps = 12,
                actualWeightInKg = 85.5,
                effort = SetEffort.HARD
            )

        val exerciseStatistics =
            ExerciseStatistics(
                id = exerciseStatisticsId,
                trainingExerciseId = trainingExerciseId,
                exerciseName = "Bench Press",
                attemptsStatistics = listOf(attempt)
            )

        val statistics =
            TrainingStatistics(
                id = statisticsId,
                trainingPlanId = trainingPlanId,
                totalTime = 120.seconds,
                date = Date(1_000_000L),
                exercisesStatistics = listOf(exerciseStatistics),
                note = "Strong session, felt good on bench press",
                rpe = SessionRpe.VERY_HARD
            )

        repository.save(statistics)

        val retrieved = repository.getByTrainingStatisticsId(statisticsId).first()
        assertNotNull(retrieved)
        assertEquals(statisticsId, retrieved.id)
        assertEquals(trainingPlanId, retrieved.trainingPlanId)
        assertEquals("Strong session, felt good on bench press", retrieved.note)
        assertEquals(SessionRpe.VERY_HARD, retrieved.rpe)
        assertEquals(1, retrieved.exercisesStatistics.size)

        val retrievedExercise = retrieved.exercisesStatistics.first()
        assertEquals(trainingExerciseId, retrievedExercise.trainingExerciseId)
        assertEquals("Bench Press", retrievedExercise.exerciseName)
        assertEquals(1, retrievedExercise.attemptsStatistics.size)

        val retrievedAttempt = retrievedExercise.attemptsStatistics.first()
        assertEquals(attemptId, retrievedAttempt.id)
        assertEquals(12, retrievedAttempt.actualReps)
        assertEquals(85.5, retrievedAttempt.actualWeightInKg)
        assertEquals(SetEffort.HARD, retrievedAttempt.effort)
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
                totalTime = 60.seconds,
                date = Date(100L),
                exercisesStatistics = emptyList(),
                note = "Plan 1 Session 1"
            )
        val stats2 =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = planId,
                totalTime = 90.seconds,
                date = Date(200L),
                exercisesStatistics = emptyList(),
                note = "Plan 1 Session 2"
            )
        val otherStats =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = otherPlanId,
                totalTime = 45.seconds,
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

    @Test
    fun getOverviewsByTrainingPlanIdReturnsMostRecentFirstWithComputedMetrics() = runTest {
        val targetPlanId = TrainingPlanId.create()
        val otherPlanId = TrainingPlanId.create()
        val holdExerciseId = TrainingExerciseId.create()
        val pressExerciseId = TrainingExerciseId.create()

        val olderStats =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = targetPlanId,
                totalTime = 30.minutes,
                date = Date(1_000L),
                exercisesStatistics = listOf(
                    ExerciseStatistics(
                        id = ExerciseStatisticsId.create(),
                        trainingExerciseId = holdExerciseId,
                        exerciseName = "90° Hold",
                        attemptsStatistics = emptyList()
                    )
                ),
                note = "Older session",
                rpe = SessionRpe.MODERATE
            )

        val newerStats =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = targetPlanId,
                totalTime = 54.minutes,
                date = Date(5_000L),
                exercisesStatistics = listOf(
                    ExerciseStatistics(
                        id = ExerciseStatisticsId.create(),
                        trainingExerciseId = holdExerciseId,
                        exerciseName = "90° Hold",
                        attemptsStatistics = listOf(
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = holdExerciseId,
                                time = 100.seconds,
                                set = 1,
                                completed = true,
                                actualReps = 10,
                                actualWeightInKg = 100.0
                            ),
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = holdExerciseId,
                                time = 50.seconds,
                                set = 2,
                                completed = false,
                                actualReps = 5,
                                actualWeightInKg = 100.0
                            )
                        )
                    ),
                    ExerciseStatistics(
                        id = ExerciseStatisticsId.create(),
                        trainingExerciseId = pressExerciseId,
                        exerciseName = "Incline DB Press",
                        attemptsStatistics = listOf(
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = pressExerciseId,
                                time = 120.seconds,
                                set = 1,
                                completed = true,
                                actualReps = 12,
                                actualWeightInKg = 25.0
                            )
                        )
                    )
                ),
                note = "Felt strong on bench",
                rpe = SessionRpe.VERY_HARD
            )

        val otherPlanStats =
            TrainingStatistics(
                id = TrainingStatisticsId.create(),
                trainingPlanId = otherPlanId,
                totalTime = 20.minutes,
                date = Date(9_000L),
                exercisesStatistics = emptyList()
            )

        repository.save(olderStats)
        repository.save(newerStats)
        repository.save(otherPlanStats)

        val overviews = repository.getOverviewsByTrainingPlanId(targetPlanId).asSnapshot()

        assertEquals(2, overviews.size)
        assertEquals(newerStats.id, overviews[0].id)
        assertEquals(olderStats.id, overviews[1].id)

        val latestOverview = overviews[0]
        assertEquals(54.minutes, latestOverview.totalTime)
        assertEquals(1800.0, latestOverview.totalVolumeKg, 0.01)
        assertEquals(2, latestOverview.completedSetsCount)
        assertEquals(3, latestOverview.totalSetsCount)
        assertEquals(2.0 / 3.0, latestOverview.completenessRate, 0.001)
        assertEquals(SessionRpe.VERY_HARD, latestOverview.rpe)
        assertEquals("Felt strong on bench", latestOverview.note)
        assertEquals(2, latestOverview.exerciseOverviews.size)

        val holdOverview = latestOverview.exerciseOverviews.first {
            it.trainingExerciseId == holdExerciseId
        }
        assertEquals("90° Hold", holdOverview.exerciseName)
        assertEquals(150.seconds, holdOverview.totalTime)
        assertEquals(0.5, holdOverview.completenessRate, 0.001)

        val olderOverview = overviews[1]
        assertEquals(0, olderOverview.totalSetsCount)
        assertEquals(0.0, olderOverview.completenessRate, 0.001)
        assertEquals(0.0, olderOverview.exerciseOverviews.single().completenessRate, 0.001)
    }

    @Test
    fun getByTrainingStatisticsIdReturnsNullForUnknownIdAndSortsSetsAscending() = runTest {
        assertNull(repository.getByTrainingStatisticsId(TrainingStatisticsId.create()).first())

        val planId = TrainingPlanId.create()
        val exerciseId = TrainingExerciseId.create()
        val statsId = TrainingStatisticsId.create()

        val statistics =
            TrainingStatistics(
                id = statsId,
                trainingPlanId = planId,
                totalTime = 10.minutes,
                date = Date(2_000L),
                exercisesStatistics = listOf(
                    ExerciseStatistics(
                        id = ExerciseStatisticsId.create(),
                        trainingExerciseId = exerciseId,
                        exerciseName = "Archer Push-Up",
                        attemptsStatistics = listOf(
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = exerciseId,
                                time = 30.seconds,
                                set = 3,
                                completed = true,
                                actualReps = 8
                            ),
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = exerciseId,
                                time = 30.seconds,
                                set = 1,
                                completed = true,
                                actualReps = 10
                            ),
                            ExerciseAttemptStatistics(
                                id = ExerciseAttemptStatisticsId.create(),
                                trainingExerciseId = exerciseId,
                                time = 30.seconds,
                                set = 2,
                                completed = true,
                                actualReps = 9
                            )
                        )
                    )
                )
            )

        repository.save(statistics)

        val retrieved = repository.getByTrainingStatisticsId(statsId).first()
        assertNotNull(retrieved)
        val setsOrder = retrieved.exercisesStatistics.single().attemptsStatistics.map { it.set }
        assertEquals(listOf(1, 2, 3), setsOrder)
    }
}
