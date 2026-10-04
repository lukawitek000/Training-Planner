package com.lukasz.witkowski.training.planner.training.infrastructure

import android.content.Context
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.training.TestData
import com.lukasz.witkowski.training.planner.training.TestData.CARDIO_ENDURANCE_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.FULL_BODY_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.TestData.PLANK_SNAPSHOT
import com.lukasz.witkowski.training.planner.training.TestData.RUNNING_SNAPSHOT
import com.lukasz.witkowski.training.planner.training.TestData.UPPER_BODY_STRENGTH_TRAINING_PLAN
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
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

        val actual = repository.getAll().last()

        assertEquals(
            TestData.TRAINING_PLAN_OVERVIEWS_LIST,
            actual,
        )
    }


    @Test
    fun `TrainingPlan is properly updated`() = runTest {
        givenSaveTrainingPlansList()
        val expectedPlan = FULL_BODY_TRAINING_PLAN.copy(
            title = "Update title",
            description = "Updated description",
            exercises = (FULL_BODY_TRAINING_PLAN.exercises - PLANK_SNAPSHOT).map {
                it.copy(
                    description = "Updated: ${it.description}",
                    categories = it.categories + TestData.CATEGORY_SHOULDERS
                )
            } + RUNNING_SNAPSHOT
        )

        val result = repository.update(expectedPlan)
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
            val result = repository.save(trainingPlan = it)
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
