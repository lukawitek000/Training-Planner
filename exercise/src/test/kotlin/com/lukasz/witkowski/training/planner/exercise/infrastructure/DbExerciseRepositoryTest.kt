package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.paging.testing.asSnapshot
import androidx.room3.Room
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DbExerciseRepositoryTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }

    private lateinit var db: ExerciseDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var repository: DbExerciseRepository
    private val ioDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            context = context, klass = ExerciseDatabase::class.java
        ).build()
        exerciseDao = db.exerciseDao()
        repository = DbExerciseRepository(
            exerciseDao = exerciseDao,
            ioDispatcher = ioDispatcher
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert and read exercise from database`() = runTest {
        val expectedDetails = TestData.SQUATS_DETAILS
        val isSuccess = repository.insert(expectedDetails)
        assertTrue(isSuccess)

        val actualExerciseDetails =
            repository.getExerciseDetailsById(expectedDetails.exercise.id).first()
        assertEquals(expectedDetails, actualExerciseDetails)
    }

    @Test
    fun `delete and read exercise from database`() = runTest {
        val details = TestData.SQUATS_DETAILS
        val id = details.exercise.id
        val isSuccess = repository.insert(details)
        assertTrue(isSuccess)

        val deleted = repository.delete(id)
        assertTrue(deleted)


        val result = repository.getExerciseDetailsById(id).first()
        assertNull(result)
    }

    @Test
    fun `update and read exercise from database`() = runTest {
        repository.insert(TestData.PULL_UPS_DETAILS)
        val details = TestData.SQUATS_DETAILS
        val id = details.exercise.id
        val isSuccess = repository.insert(details)
        assertTrue(isSuccess)

        val updatedDetails = TestData.SQUATS_DETAILS.copy(
            exercise = TestData.SQUATS_DETAILS.exercise.copy(
                name = "New name",
                categories = TestData.PULL_UPS_DETAILS.exercise.categories
            ),
            beginnerRecommendation = TestData.SQUATS_DETAILS.beginnerRecommendation.copy(
                sets = 10,
                reps = 100,
            )
        )
        val updated = repository.updateExercise(updatedDetails)
        assertTrue(updated)


        val result = repository.getExerciseDetailsById(id).first()
        assertEquals(updatedDetails, result)
    }

    @Test
    fun `quering by category exercises returns only expected exercises`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(
            categories = listOf(TestData.CATEGORY_BICEPS),
            query = ""
        )
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(2, exercises.size)
        val expected = setOf(TestData.PUSH_UPS_EXERCISE, TestData.PULL_UPS_EXERCISE)
        assertEquals(expected, exercises.toSet())
    }

    @Test
    fun `quering by multiple categories returns only expected exercises in most matching order`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(
            categories = listOf(TestData.CATEGORY_BICEPS, TestData.CATEGORY_CHEST),
            query = ""
        )
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(2, exercises.size)
        val expected = listOf(TestData.PUSH_UPS_EXERCISE, TestData.PULL_UPS_EXERCISE)
        assertEquals(expected, exercises)
    }

    @Test
    fun `quering by category and query exercises returns only expected exercises`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(
            categories = listOf(TestData.CATEGORY_BICEPS),
            query = "push"
        )
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(1, exercises.size)
        val expected = setOf(TestData.PUSH_UPS_EXERCISE)
        assertEquals(expected, exercises.toSet())
    }

    @Test
    fun `quering by query exercises returns only expected exercises`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(
            categories = emptyList(),
            query = "BODYWEIGHT"
        )
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(2, exercises.size)
        val expected = setOf(TestData.PUSH_UPS_EXERCISE, TestData.SQUATS_EXERCISE)
        assertEquals(expected, exercises.toSet())
    }

    @Test
    fun `getExerciseDetailsById returns null for non existing exercise`() = runTest {
        val nonExistingId = TestData.EXERCISE_ID_4
        val result = repository.getExerciseDetailsById(nonExistingId).first()
        assertNull(result)
    }

    @Test
    fun `delete non existing exercise returns false`() = runTest {
        val nonExistingId = TestData.EXERCISE_ID_4
        val deleted = repository.delete(nonExistingId)
        assertFalse(deleted)
    }

    @Test
    fun `update non existing exercise returns false`() = runTest {
        val details = TestData.PUSH_UPS_DETAILS
        val updated = repository.updateExercise(details)
        assertFalse(updated)
    }

    @Test
    fun `updating exercise categories removes old category cross references`() = runTest {
        val initialDetails = TestData.PUSH_UPS_DETAILS
        insertAllTestExercises()

        val updatedDetails = initialDetails.copy(
            exercise = initialDetails.exercise.copy(
                categories = setOf(TestData.CATEGORY_LEGS)
            )
        )
        val isUpdated = repository.updateExercise(updatedDetails)
        assertTrue(isUpdated)

        val chestQuery = ExerciseQuery(categories = listOf(TestData.CATEGORY_CHEST), query = "")
        val chestExercises = repository.queryExercises(chestQuery).asSnapshot()
        assertTrue(chestExercises.isEmpty())

        val legsQuery = ExerciseQuery(categories = listOf(TestData.CATEGORY_LEGS), query = "")
        val legsExercises = repository.queryExercises(legsQuery).asSnapshot()
        assertEquals(2, legsExercises.size)
        val expected = setOf(TestData.SQUATS_EXERCISE, updatedDetails.exercise)
        assertEquals(expected, legsExercises.toSet())
    }

    @Test
    fun `quering with empty query and empty categories returns all exercises`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(categories = emptyList(), query = "")
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(TestData.EXERCISES_LIST.size, exercises.size)
        assertEquals(TestData.EXERCISES_LIST.toSet(), exercises.toSet())
    }

    @Test
    fun `quering with non matching search criteria returns empty list`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(categories = listOf(TestData.CATEGORY_CARDIO), query = "nonexistent")
        val exercises = repository.queryExercises(query).asSnapshot()
        assertTrue(exercises.isEmpty())
    }

    @Test
    fun `quering by description text returns matching exercise`() = runTest {
        insertAllTestExercises()
        val query = ExerciseQuery(categories = emptyList(), query = "glutes")
        val exercises = repository.queryExercises(query).asSnapshot()
        assertEquals(1, exercises.size)
        assertEquals(TestData.SQUATS_EXERCISE, exercises.first())
    }

    private suspend fun insertAllTestExercises() {
        TestData.EXERCISE_DETAILS_LIST.forEach {
            repository.insert(it)
        }
    }
}