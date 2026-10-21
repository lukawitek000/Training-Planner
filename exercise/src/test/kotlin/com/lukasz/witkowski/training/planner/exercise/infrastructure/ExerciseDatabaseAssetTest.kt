package com.lukasz.witkowski.training.planner.exercise.infrastructure

import android.content.Context
import androidx.room3.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

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
            assertNotNull("Bundled exercise imageId should not be null: ${exercise.name}", exercise.imageId)

            val details = exerciseDao.getExerciseDetailsById(exercise.exerciseId).first()
            assertNotNull("Details should exist for ${exercise.name}", details)
            assertTrue("Bundled exercise should have categories: ${exercise.name}", details!!.categories.isNotEmpty())
            assertTrue("Bundled exercise should have recommendations: ${exercise.name}", details.recommendations.isNotEmpty())
            assertTrue("Bundled exercise should have images: ${exercise.name}", details.images.isNotEmpty())
        }
    }

    @Test
    fun verifyCategoriesAreLoaded() = runTest {
        val categoryDao = db.categoryDao()
        val categories = categoryDao.getAll().first()
        assertTrue("Should contain categories", categories.isNotEmpty())
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
}
