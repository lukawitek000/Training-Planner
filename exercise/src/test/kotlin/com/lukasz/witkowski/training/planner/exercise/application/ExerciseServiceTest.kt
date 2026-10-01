package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.FakeExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.toImageConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseServiceTest {
    private val exerciseRepository = FakeExerciseRepository()
    private val imageStorage = mockk<ImageStorage>()
    private val service =
        ExerciseService(
            exerciseRepository = exerciseRepository,
            imageStorage = imageStorage,
        )

    @Test
    fun `save exercise calls exercise repository and image storage`() =
        runTest {
            coEvery { imageStorage.saveImage(any()) } returns TestData.SAMPLE_IMAGE_REFERENCE
            val exerciseId =
                service.saveExercise(
                    exerciseConfiguration = TestData.PUSH_UPS_CONFIGURATION,
                )
            assertEquals(
                TestData.PUSH_UPS_DETAILS.copy(
                    exercise = TestData.PUSH_UPS_EXERCISE.copy(id = exerciseId),
                ),
                exerciseRepository.exercises.first(),
            )
            val imageConfig =
                TestData.SAMPLE_IMAGE_BYTE_ARRAY.toImageConfiguration(
                    ownerId = exerciseId.value,
                )
            coVerify(exactly = 1) { imageStorage.saveImage(imageConfig) }
        }

    @Test
    fun `save exercise calls exercise repository and no image storage`() =
        runTest {
            val exerciseId =
                service.saveExercise(
                    exerciseConfiguration = TestData.SQUATS_CONFIGURATION,
                )
            assertEquals(
                TestData.SQUATS_DETAILS.copy(
                    exercise = TestData.SQUATS_EXERCISE.copy(id = exerciseId),
                ),
                exerciseRepository.exercises.first(),
            )
            coVerify(exactly = 0) { imageStorage.saveImage(any()) }
        }

    @Test
    fun `update exercise calls exercise repository and image storage`() =
        runTest {
            coEvery { imageStorage.saveImage(any()) } returns TestData.SAMPLE_IMAGE_REFERENCE
            val exerciseId = TestData.EXERCISE_ID_1
            val updatedId =
                service.updateExercise(
                    exerciseConfiguration = TestData.PUSH_UPS_CONFIGURATION,
                    exerciseId = exerciseId,
                )
            assertEquals(exerciseId, updatedId)
            assertEquals(
                TestData.PUSH_UPS_DETAILS.copy(
                    exercise = TestData.PUSH_UPS_EXERCISE.copy(id = exerciseId),
                ),
                exerciseRepository.exercises.first(),
            )
            val imageConfig =
                TestData.SAMPLE_IMAGE_BYTE_ARRAY.toImageConfiguration(
                    ownerId = exerciseId.value,
                )
            coVerify(exactly = 1) { imageStorage.saveImage(imageConfig) }
        }

    @Test
    fun `update exercise calls exercise repository and no image storage`() =
        runTest {
            val exerciseId = TestData.EXERCISE_ID_2
            val updatedId =
                service.updateExercise(
                    exerciseConfiguration = TestData.SQUATS_CONFIGURATION,
                    exerciseId = exerciseId,
                )
            assertEquals(exerciseId, updatedId)
            assertEquals(
                TestData.SQUATS_DETAILS.copy(
                    exercise = TestData.SQUATS_EXERCISE.copy(id = exerciseId),
                ),
                exerciseRepository.exercises.first(),
            )
            coVerify(exactly = 0) { imageStorage.saveImage(any()) }
        }
}
