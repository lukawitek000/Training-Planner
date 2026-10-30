package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.Room
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ExerciseDatabaseAssetTest {

    private val context: Context by lazy { RuntimeEnvironment.getApplication() }
    private lateinit var db: ExerciseDatabase
    private val dbName = "test_bundled_exercises.db"

    @Before
    fun setUp() {
        context.deleteDatabase(dbName)
        val dbFile = File("src/main/assets/database/exercises.db")
        db = Room.databaseBuilder(
            context,
            ExerciseDatabase::class.java,
            dbName
        ).createFromFile(dbFile)
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun verifyBundledDatabaseLoadsSuccessfullyWithExpectedExercisesAndImages() = runTest {
        val exerciseDao = db.exerciseDao()
        val allExercises = exerciseDao.getAll().first()

        assertEquals("Should contain 71 bundled exercises", 71, allExercises.size)

        allExercises.forEach { exercise ->
            assertNotNull(
                "Bundled exercise imageId should not be null: ${exercise.name}",
                exercise.imageId
            )
            assertEquals(
                "Bundled exercise sourceType should be BUNDLED",
                DbExercise.SOURCE_TYPE_BUNDLED,
                exercise.sourceType
            )
            assertEquals(
                "Bundled exercise sharing should be COMMUNITY",
                DbExercise.SHARING_COMMUNITY,
                exercise.sharing
            )
            assertEquals("Bundled exercise revision should be 1", 1L, exercise.revision)
            assertEquals(
                "Bundled exercise syncState should be SYNCED",
                DbExercise.SYNC_STATE_SYNCED,
                exercise.syncState
            )
            assertNull("Bundled exercise owner should be null", exercise.owner)

            val details = exerciseDao.getExerciseDetailsById(exercise.exerciseId).first()
            assertNotNull("Details should exist for ${exercise.name}", details)
            assertTrue(
                "Bundled exercise should have categories: ${exercise.name}",
                details!!.categories.isNotEmpty()
            )
            assertTrue(
                "Bundled exercise should have recommendations: ${exercise.name}",
                details.recommendations.isNotEmpty()
            )
            assertTrue(
                "Bundled exercise should have images: ${exercise.name}",
                details.images.isNotEmpty()
            )
        }
    }

    @Test
    fun verifyCategoriesAreLoaded() = runTest {
        val categoryDao = db.categoryDao()
        val categories = categoryDao.getAll().first()
        assertEquals("Should contain 9 categories", 9, categories.size)
    }

    @Test
    fun verifySampleExercisesHaveImages() = runTest {
        val exerciseDao = db.exerciseDao()
        val allExercises = exerciseDao.getAll().first()

        val pushUp = allExercises.firstOrNull { it.name == "Push-Up" }
        assertNotNull("Push-Up should be present", pushUp)

        val pushUpDetails = exerciseDao.getExerciseDetailsById(pushUp!!.exerciseId).first()
        assertNotNull("Push-Up details should be present", pushUpDetails)
        assertEquals("Push-Up should have 2 images", 2, pushUpDetails!!.images.size)
    }

    @Test
    fun verifyHoldExercisesHaveTimeInSecondsAndSingleRep() = runTest {
        val exerciseDao = db.exerciseDao()
        val allExercises = exerciseDao.getAll().first()

        val plank = allExercises.firstOrNull { it.name == "Plank" }
        assertNotNull("Plank exercise should be present", plank)

        val plankDetails = exerciseDao.getExerciseDetailsById(plank!!.exerciseId).first()
        assertNotNull("Plank details should be present", plankDetails)
        assertEquals(3, plankDetails!!.recommendations.size)

        plankDetails.recommendations.forEach { recommendation ->
            assertEquals("Plank reps should be 1 for hold exercises", 1, recommendation.reps)
            assertNotNull("Plank timeInSeconds should not be null", recommendation.timeInSeconds)
            assertTrue("Plank timeInSeconds should be > 0", recommendation.timeInSeconds!! > 0)
        }
    }

    @Test
    fun verifyRepsExercisesHaveRepsAndNullTimeInSeconds() = runTest {
        val exerciseDao = db.exerciseDao()
        val allExercises = exerciseDao.getAll().first()

        val pullUp = allExercises.firstOrNull { it.name == "Pull-Up" }
        assertNotNull("Pull-Up exercise should be present", pullUp)

        val pullUpDetails = exerciseDao.getExerciseDetailsById(pullUp!!.exerciseId).first()
        assertNotNull("Pull-Up details should be present", pullUpDetails)
        assertEquals(3, pullUpDetails!!.recommendations.size)

        pullUpDetails.recommendations.forEach { recommendation ->
            assertTrue("Pull-Up reps should be > 1", recommendation.reps > 1)
            assertNull("Pull-Up timeInSeconds should be null", recommendation.timeInSeconds)
        }
    }
}
