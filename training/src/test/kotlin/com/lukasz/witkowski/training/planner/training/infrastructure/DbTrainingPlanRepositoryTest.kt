package com.lukasz.witkowski.training.planner.training.infrastructure

import android.content.Context
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.TestData.CARDIO_ENDURANCE_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.CATEGORY_LEGS
import com.lukasz.witkowski.training.planner.training.TestData.FULL_BODY_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.PLANK_SNAPSHOT
import com.lukasz.witkowski.training.planner.training.TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.toTrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.SortDirection
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingQuery
import com.lukasz.witkowski.training.planner.training.domain.TrainingSortBy
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class DbTrainingPlanRepositoryTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }
    private lateinit var db: TrainingPlanDatabase
    private lateinit var repository: DbTrainingPlanRepository
    private val ioDispatcher = UnconfinedTestDispatcher()

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
                dispatcher = ioDispatcher
            )
    }

    @Test
    fun `TrainingPlan is saved, and read from DB`() = runTest {
        givenSaveTrainingPlansList()

        assertTrainingPlanList()
    }

    @Test
    fun `TrainingPlans are saved, and read from DB the overviews is successful`() = runTest {
        givenSaveTrainingPlansList()

        val actual = repository.getAll(TrainingQuery.DEFAULT).last()

        assertEquals(
            TestData.TRAINING_PLAN_OVERVIEWS_LIST,
            actual,
        )
    }

    @Test
    fun `TrainingPlans are properly filtered by query`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "Full",
            selectedCategories = emptySet(),
            sortBy = TrainingSortBy.Modified(SortDirection.ASCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(1, actual.size)
        assertEquals(listOf(TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW), actual)
    }


    @Test
    fun `TrainingPlans are properly filtered by categories`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(
                TestData.CATEGORY_CHEST, TestData.CATEGORY_ARMS
            ),
            sortBy = TrainingSortBy.Modified(SortDirection.ASCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(2, actual.size)
        assertEquals(
            listOf(
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW,
                TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW
            ),
            actual
        )
    }

    @Test
    fun `TrainingPlans are properly filtered by query and categories`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "Body",
            selectedCategories = setOf(CATEGORY_LEGS),
            sortBy = TrainingSortBy.Modified(SortDirection.ASCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(1, actual.size)
        assertEquals(
            listOf(
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW,
            ),
            actual
        )
    }

    @Test
    fun `TrainingPlans are properly sorted by last modification ascending`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(),
            sortBy = TrainingSortBy.Modified(SortDirection.ASCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(3, actual.size)
        assertEquals(TestData.TRAINING_PLAN_OVERVIEWS_LIST, actual)
    }

    @Test
    fun `TrainingPlans are properly sorted by last modification descending`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(),
            sortBy = TrainingSortBy.Modified(SortDirection.DESCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(3, actual.size)
        assertEquals(TestData.TRAINING_PLAN_OVERVIEWS_LIST.reversed(), actual)
    }

    @Test
    fun `TrainingPlans are properly sorted by last usage ascending`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(),
            sortBy = TrainingSortBy.Used(SortDirection.ASCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(3, actual.size)
        assertEquals(
            listOf(
                TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW,
                TestData.CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW,
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW
            ),
            actual
        )
    }

    @Test
    fun `TrainingPlans are properly sorted by last usage descending`() = runTest {
        givenSaveTrainingPlansList()
        val query = TrainingQuery(
            searchQuery = "",
            selectedCategories = setOf(),
            sortBy = TrainingSortBy.Used(SortDirection.DESCENDING)
        )

        val actual = repository.getAll(query).last()

        assertEquals(3, actual.size)
        assertEquals(
            listOf(
                TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW,
                TestData.CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW,
                TestData.FULL_BODY_TRAINING_PLAN_OVERVIEW
            ).asReversed(),
            actual
        )
    }

    @Test
    fun `TrainingPlan is properly updated`() = runTest {
        givenSaveTrainingPlansList()
        val expectedPlan = FULL_BODY_TRAINING_PLAN.copy(
            title = "Update title",
            description = "Updated description",
            exercises = FULL_BODY_TRAINING_PLAN.exercises.filter { it.exercise != PLANK_SNAPSHOT }.map {
                it.copy(
                    exercise = it.exercise.copy(
                        description = "Updated: ${it.exercise.description}",
                        categories = it.exercise.categories + TestData.CATEGORY_SHOULDERS
                    )
                )
            } + TestData.TRAINING_EXERCISE_RUNNING
        )

        val result = repository.update(expectedPlan.toTrainingPlanConfiguration(), expectedPlan.id)
        assertTrue(result.isSuccess, message = "Failed to update TrainingPlan")

        assertTrainingPlanList(
            expected = listOf(
                expectedPlan,
                CARDIO_ENDURANCE_TRAINING_PLAN,
                UPPER_BODY_STRENGTH_TRAINING_PLAN,
            )
        )
    }

    @Test
    fun `TrainingPlan is properly deleted`() = runTest {
        givenSaveTrainingPlansList()

        val id = FULL_BODY_TRAINING_PLAN.id
        val result = repository.delete(id)
        assertNotNull(result.getOrNull())

        assertTrainingPlanList(
            expected = TestData.TRAINING_PLANS_LIST.filter { it.id != id }
        )
    }

    private suspend fun givenSaveTrainingPlansList() {
        TestData.TRAINING_PLANS_LIST.forEach {
            val result = repository.save(it.toTrainingPlanConfiguration(), it.id)
            assertTrue(result.isSuccess, message = "Failed save for ${it.title}")
            assertEquals(it.id, result.getOrThrow())
        }
    }

    private suspend fun assertTrainingPlanList(expected: List<TrainingPlan> = TestData.TRAINING_PLANS_LIST) {
        expected.forEach {
            val actual = repository.getTrainingPlanById(it.id).last()
            assertEquals(it, actual, "Failed read for ${it.title}")
        }
    }
}
