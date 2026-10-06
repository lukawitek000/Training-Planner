package com.lukasz.witkowski.training.planner.training.infrastructure

import android.content.Context
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.TestData.CATEGORY_LEGS
import com.lukasz.witkowski.training.planner.training.TestData.FULL_BODY_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.PLANK_SNAPSHOT
import com.lukasz.witkowski.training.planner.training.TestData.toTrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
class DbTrainingPlanRepositoryTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }
    private lateinit var db: TrainingPlanDatabase
    private lateinit var repository: DbTrainingPlanRepository
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val testTimeProvider = TestTimeProvider()

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    context = context,
                    klass = TrainingPlanDatabase::class.java,
                ).build()
        val dao = db.trainingPlanDao()
        repository =
            DbTrainingPlanRepository(
                trainingPlanDao = dao,
                timeProvider = testTimeProvider,
                dispatcher = ioDispatcher
            )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `TrainingPlan is saved, and read from DB`() = runTest {
        val expectedLastModification = Instant.parse("2026-09-10T12:00:00Z")
        testTimeProvider.instant = expectedLastModification
        givenSaveTrainingPlansList()

        TestData.TRAINING_PLANS_LIST.forEach {
            val actual = repository.getTrainingPlanById(it.id).first()
            assertEquals(
                it.copy(
                    lastModification = expectedLastModification,
                    lastSession = null,
                ),
                actual,
                "Failed read for ${it.title}"
            )
        }
    }

    @Test
    fun `TrainingPlans are saved, and read from DB the overviews is successful`() = runTest {
        val expectedLastModification = Instant.parse("2026-09-12T14:00:00Z")
        testTimeProvider.instant = expectedLastModification
        givenSaveTrainingPlansList()

        val result = repository.getAll(TrainingQuery.DEFAULT).first()

        result.forEach { actual ->
            val expected = TestData.TRAINING_PLAN_OVERVIEWS_LIST.first { it.id == actual.id }
            assertEquals(
                expected.copy(
                    lastModification = expectedLastModification,
                    lastSession = null,
                ),
                actual
            )
        }
    }

    @Test
    fun `TrainingPlans are properly filtered by query`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val query = TrainingQuery(searchQuery = "Cardio", selectedCategories = emptySet())

        val actual = repository.getAll(query).first()

        assertEquals(1, actual.size)
        assertEquals(
            listOf(TestData.CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW.copy(lastSession = null)),
            actual
        )
    }

    @Test
    fun `Filtering by query or category returns empty list when no training plan matches`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val queryNoMatch = TrainingQuery(searchQuery = "NonExistentSearchString", selectedCategories = emptySet())
        val actualNoMatch = repository.getAll(queryNoMatch).first()
        assertTrue(actualNoMatch.isEmpty())

        val queryNoCatMatch = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(TestData.CATEGORY_SHOULDERS)
        )
        val actualNoCatMatch = repository.getAll(queryNoCatMatch).first()
        assertTrue(actualNoCatMatch.isEmpty())
    }


    @Test
    fun `TrainingPlans are properly filtered by categories`() = runTest {
        givenSaveTrainingPlansList(
            preSave = {
                testTimeProvider.instant = it.lastModification
            }
        )
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(
                TestData.CATEGORY_CHEST, TestData.CATEGORY_ARMS
            ),
        )

        val actual = repository.getAll(query).first()

        assertEquals(2, actual.size)
        assertEquals(
            listOf(
                TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW.copy(lastSession = null),
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW.copy(lastSession = null)
            ),
            actual
        )
    }

    @Test
    fun `TrainingPlans are properly filtered by query and categories`() = runTest {
        givenSaveTrainingPlansList(
            preSave = {
                testTimeProvider.instant = it.lastModification
            }
        )
        val query = TrainingQuery(
            searchQuery = "Body",
            selectedCategories = setOf(CATEGORY_LEGS),
        )

        val actual = repository.getAll(query).first()

        assertEquals(1, actual.size)
        assertEquals(
            listOf(
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW.copy(lastSession = null),
            ),
            actual
        )
    }

    @Test
    fun `TrainingPlans are properly sorted by last modification, most recent first`() = runTest {
        givenSaveTrainingPlansList(
            preSave = {
                testTimeProvider.instant = it.lastModification
            }
        )

        val result = repository.getAll(TrainingQuery.DEFAULT).first()

        assertEquals(3, result.size)
        listOf(
            TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW,
            TestData.CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW,
            TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW,
        ).zip(result).forEach { (expected, actual) ->
            assertEquals(expected.copy(lastSession = null), actual)
        }
    }

    @Test
    fun `TrainingPlans are properly sorted by last session with fallback to last modification`() = runTest {
        givenSaveTrainingPlansList(
            preSave = {
                testTimeProvider.instant = it.lastModification
            }
        )
        val useInstant = Instant.parse("2026-10-04T12:00:00Z")
        testTimeProvider.instant = useInstant
        val resultPlan = repository.useTrainingPlan(TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW.id)
        assertTrue(resultPlan.isSuccess, "Failed to use TrainingPlan ${resultPlan.exceptionOrNull()?.message}")
        assertEquals(FULL_BODY_TRAINING_PLAN.copy(
            lastSession = null
        ), resultPlan.getOrThrow())

        val result = repository.getAll(TrainingQuery.DEFAULT).first()

        assertEquals(3, result.size)
        listOf(
            TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW.copy(lastSession = useInstant),
            TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW.copy(lastSession = null),
            TestData.CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW.copy(lastSession = null),
        ).zip(result).forEach { (expected, actual) ->
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `TrainingPlan is properly updated`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val updateInstant = Instant.parse("2026-10-06T12:00:00Z")
        testTimeProvider.instant = updateInstant

        val expectedPlan = FULL_BODY_TRAINING_PLAN.copy(
            title = "Update title",
            description = "Updated description",
            exercises = FULL_BODY_TRAINING_PLAN.exercises.filter { it.exercise != PLANK_SNAPSHOT }
                .map {
                    it.copy(
                        exercise = it.exercise.copy(
                            description = "Updated: ${it.exercise.description}",
                            categories = it.exercise.categories + TestData.CATEGORY_SHOULDERS
                        )
                    )
                } + TestData.TRAINING_EXERCISE_RUNNING,
            lastModification = updateInstant,
            lastSession = null,
        )

        val result = repository.update(expectedPlan.toTrainingPlanConfiguration(), expectedPlan.id)
        assertTrue(result.isSuccess, message = "Failed to update TrainingPlan")

        val actualUpdatedPlan = repository.getTrainingPlanById(expectedPlan.id).first()
        assertEquals(expectedPlan, actualUpdatedPlan)
    }

    @Test
    fun `Exercise order is updated and preserved`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val updateInstant = Instant.parse("2026-10-06T12:00:00Z")
        testTimeProvider.instant = updateInstant

        val originalPlan = repository.getTrainingPlanById(FULL_BODY_TRAINING_PLAN.id).first()!!
        val reorderedExercises = originalPlan.exercises.reversed()
        val reorderedPlanConfig = originalPlan.toTrainingPlanConfiguration().copy(
            exercises = reorderedExercises
        )

        val result = repository.update(reorderedPlanConfig, originalPlan.id)
        assertTrue(result.isSuccess)

        val updatedPlan = repository.getTrainingPlanById(originalPlan.id).first()!!
        assertEquals(reorderedExercises, updatedPlan.exercises)
    }

    @Test
    fun `Exercises details are updated`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val originalPlan = repository.getTrainingPlanById(FULL_BODY_TRAINING_PLAN.id).first()!!
        val updatedExercises = originalPlan.exercises.map { exercise ->
            exercise.copy(
                repetitions = exercise.repetitions + 5,
                sets = exercise.sets + 1,
                weightInKg = (exercise.weightInKg ?: 0) + 10,
                exercise = exercise.exercise.copy(
                    description = "Updated description for ${exercise.exercise.name}",
                    categories = exercise.exercise.categories + TestData.CATEGORY_SHOULDERS
                )
            )
        }
        val updatedConfig = originalPlan.toTrainingPlanConfiguration().copy(exercises = updatedExercises)

        val result = repository.update(updatedConfig, originalPlan.id)
        assertTrue(result.isSuccess)

        val updatedPlan = repository.getTrainingPlanById(originalPlan.id).first()!!
        assertEquals(updatedExercises, updatedPlan.exercises)
    }

    @Test
    fun `Modification date is updated on plan update`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val updateInstant = Instant.parse("2026-10-06T15:30:00Z")
        testTimeProvider.instant = updateInstant

        val originalPlan = repository.getTrainingPlanById(FULL_BODY_TRAINING_PLAN.id).first()!!
        val updatedConfig = originalPlan.toTrainingPlanConfiguration().copy(title = "New Title")

        val result = repository.update(updatedConfig, originalPlan.id)
        assertTrue(result.isSuccess)

        val updatedPlan = repository.getTrainingPlanById(originalPlan.id).first()!!
        assertEquals(updateInstant, updatedPlan.lastModification)
    }

    @Test
    fun `Updating non-existent training plan returns failure`() = runTest {
        val nonExistentId = TrainingPlanId.create()
        val config = FULL_BODY_TRAINING_PLAN.toTrainingPlanConfiguration()

        val result = repository.update(config, nonExistentId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `TrainingPlan is properly deleted`() = runTest {
        givenSaveTrainingPlansList()

        val id = FULL_BODY_TRAINING_PLAN.id
        val dao = db.trainingPlanDao()
        assertTrue(dao.getExercisesCountForTrainingPlan(id.toString()) > 0)
        assertTrue(dao.getCategoriesCountForTrainingPlan(id.toString()) > 0)

        val result = repository.delete(id)
        assertNotNull(result.getOrNull())

        val plan = repository.getTrainingPlanById(id).first()
        assertEquals(null, plan)

        assertEquals(0, dao.getExercisesCountForTrainingPlan(id.toString()))
        assertEquals(0, dao.getCategoriesCountForTrainingPlan(id.toString()))
    }

    @Test
    fun `Using non-existent training plan returns failure`() = runTest {
        val nonExistentId = TrainingPlanId.create()
        val result = repository.useTrainingPlan(nonExistentId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `Deleting non-existent training plan succeeds without error`() = runTest {
        val nonExistentId = TrainingPlanId.create()
        val result = repository.delete(nonExistentId)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getTrainingPlanById emits null for non-existent ID and updates when deleted`() = runTest {
        val nonExistentId = TrainingPlanId.create()
        val initial = repository.getTrainingPlanById(nonExistentId).first()
        assertEquals(null, initial)

        givenSaveTrainingPlansList()
        val planId = FULL_BODY_TRAINING_PLAN.id
        val savedPlan = repository.getTrainingPlanById(planId).first()
        assertNotNull(savedPlan)

        repository.delete(planId)
        val afterDelete = repository.getTrainingPlanById(planId).first()
        assertEquals(null, afterDelete)
    }

    @Test
    fun `Updating training plan with added or removed exercises`() = runTest {
        givenSaveTrainingPlansList(
            preSave = { testTimeProvider.instant = it.lastModification }
        )
        val updateInstant = Instant.parse("2026-10-06T14:00:00Z")
        testTimeProvider.instant = updateInstant

        // 1. Remove exercises except one
        val planWithSingleExercise = FULL_BODY_TRAINING_PLAN.copy(
            exercises = listOf(TestData.TRAINING_EXERCISE_PUSH_UPS),
            lastModification = updateInstant,
            lastSession = null,
        )
        val updateResult1 = repository.update(planWithSingleExercise.toTrainingPlanConfiguration(), planWithSingleExercise.id)
        assertTrue(updateResult1.isSuccess)

        val actual1 = repository.getTrainingPlanById(planWithSingleExercise.id).first()
        assertEquals(planWithSingleExercise, actual1)

        // 2. Add multiple exercises back
        val planWithMoreExercises = planWithSingleExercise.copy(
            exercises = listOf(
                TestData.TRAINING_EXERCISE_PUSH_UPS,
                TestData.TRAINING_EXERCISE_SQUATS,
                TestData.TRAINING_EXERCISE_RUNNING
            ),
            lastModification = updateInstant,
            lastSession = null,
        )
        val updateResult2 = repository.update(planWithMoreExercises.toTrainingPlanConfiguration(), planWithMoreExercises.id)
        assertTrue(updateResult2.isSuccess)

        val actual2 = repository.getTrainingPlanById(planWithMoreExercises.id).first()
        assertEquals(planWithMoreExercises, actual2)
    }

    @Test
    fun `Training plan with 0 exercises can be saved, read, updated, and deleted`() = runTest {
        val saveInstant = Instant.parse("2026-10-06T10:00:00Z")
        testTimeProvider.instant = saveInstant

        val emptyPlanId = TrainingPlanId.create()
        val emptyPlanConfig = TrainingPlanConfiguration(
            title = "Empty Plan",
            description = "No exercises",
            exercises = emptyList(),
            restTime = 30.seconds
        )

        val saveResult = repository.save(emptyPlanConfig, emptyPlanId)
        assertTrue(saveResult.isSuccess)

        val readPlan = repository.getTrainingPlanById(emptyPlanId).first()
        assertNotNull(readPlan)
        assertEquals("Empty Plan", readPlan.title)
        assertTrue(readPlan.exercises.isEmpty())

        val overviews = repository.getAll(TrainingQuery.DEFAULT).first()
        assertTrue(overviews.any { it.id == emptyPlanId })

        val deleteResult = repository.delete(emptyPlanId)
        assertTrue(deleteResult.isSuccess)

        val afterDelete = repository.getTrainingPlanById(emptyPlanId).first()
        assertEquals(null, afterDelete)
    }

    private suspend fun givenSaveTrainingPlansList(
        preSave: (TrainingPlan) -> Unit = {}
    ) {
        TestData.TRAINING_PLANS_LIST.forEach {
            preSave(it)
            val result = repository.save(it.toTrainingPlanConfiguration(), it.id)
            assertTrue(result.isSuccess, message = "Failed save for ${it.title}")
            assertEquals(it.id, result.getOrThrow())
        }
    }

    private suspend fun assertTrainingPlanList(expected: List<TrainingPlan> = TestData.TRAINING_PLANS_LIST) {
        expected.forEach {
            val actual = repository.getTrainingPlanById(it.id).first()
            assertEquals(it, actual, "Failed read for ${it.title}")
        }
    }
}
