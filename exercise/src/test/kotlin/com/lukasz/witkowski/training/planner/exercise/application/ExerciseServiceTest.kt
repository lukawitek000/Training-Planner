package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.FakeExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.toImageConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class ExerciseServiceTest {
    private val exerciseRepository = FakeExerciseRepository()
    private val imageStorage = mockk<ImageStorage>(relaxed = true)
    private val service =
        ExerciseService(
            exerciseRepository = exerciseRepository,
            imageStorage = imageStorage
        )

    private val expectedCreatedMetadata = ExerciseMetadata(
        source = ExerciseSource.UserCreated(ExerciseOwner.Local()),
        syncState = ExerciseSyncState.PENDING_UPLOAD
    )

    @Test
    fun `save exercise calls exercise repository and image storage`() = runTest {
        coEvery { imageStorage.saveImages(any()) } returns listOf(TestData.SAMPLE_IMAGE_REFERENCE)
        val exerciseId =
            service.saveExercise(
                exerciseConfiguration = TestData.PUSH_UPS_CONFIGURATION
            )
        assertEquals(
            TestData.PUSH_UPS_DETAILS.copy(
                exercise = TestData.PUSH_UPS_EXERCISE.copy(
                    id = exerciseId,
                    metadata = expectedCreatedMetadata
                )
            ),
            exerciseRepository.exercises.first()
        )
        val imageConfig =
            TestData.SAMPLE_IMAGE_BYTE_ARRAY.toImageConfiguration(
                ownerId = exerciseId.value
            )
        coVerify(exactly = 1) { imageStorage.saveImages(listOf(imageConfig)) }
    }

    @Test
    fun `save exercise calls exercise repository and no image storage`() = runTest {
        val exerciseId =
            service.saveExercise(
                exerciseConfiguration = TestData.SQUATS_CONFIGURATION
            )
        assertEquals(
            TestData.SQUATS_DETAILS.copy(
                exercise = TestData.SQUATS_EXERCISE.copy(
                    id = exerciseId,
                    metadata = expectedCreatedMetadata
                )
            ),
            exerciseRepository.exercises.first()
        )
        coVerify(exactly = 0) { imageStorage.saveImages(any()) }
    }

    @Test
    fun `update exercise calls exercise repository and image storage`() = runTest {
        coEvery { imageStorage.saveImages(any()) } returns listOf(TestData.SAMPLE_IMAGE_REFERENCE)
        val exerciseId = TestData.EXERCISE_ID_1
        val updatedId =
            service.updateExercise(
                exerciseConfiguration = TestData.PUSH_UPS_CONFIGURATION,
                exerciseId = exerciseId
            )
        assertEquals(exerciseId, updatedId)
        assertEquals(
            TestData.PUSH_UPS_DETAILS.copy(
                exercise = TestData.PUSH_UPS_EXERCISE.copy(
                    id = exerciseId,
                    metadata = expectedCreatedMetadata
                )
            ),
            exerciseRepository.exercises.first()
        )
        val imageConfig =
            TestData.SAMPLE_IMAGE_BYTE_ARRAY.toImageConfiguration(
                ownerId = exerciseId.value
            )
        coVerify(exactly = 1) { imageStorage.saveImages(listOf(imageConfig)) }
    }

    @Test
    fun `update exercise calls exercise repository and no image storage`() = runTest {
        val exerciseId = TestData.EXERCISE_ID_2
        val updatedId =
            service.updateExercise(
                exerciseConfiguration = TestData.SQUATS_CONFIGURATION,
                exerciseId = exerciseId
            )
        assertEquals(exerciseId, updatedId)
        assertEquals(
            TestData.SQUATS_DETAILS.copy(
                exercise = TestData.SQUATS_EXERCISE.copy(
                    id = exerciseId,
                    metadata = expectedCreatedMetadata
                )
            ),
            exerciseRepository.exercises.first()
        )
        coVerify(exactly = 0) { imageStorage.saveImages(any()) }
    }

    @Test
    fun `update exercise throws IllegalStateException for bundled exercise`() = runTest {
        val bundledDetails = TestData.PUSH_UPS_DETAILS.copy(
            exercise = TestData.PUSH_UPS_EXERCISE.copy(
                metadata = ExerciseMetadata(source = ExerciseSource.Bundled)
            )
        )
        exerciseRepository.insert(bundledDetails)

        assertFailsWith<IllegalStateException> {
            service.updateExercise(
                exerciseConfiguration = TestData.PUSH_UPS_CONFIGURATION,
                exerciseId = bundledDetails.exercise.id
            )
        }
    }

    @Test
    fun `delete exercise throws IllegalStateException for bundled exercise`() = runTest {
        val bundledDetails = TestData.PUSH_UPS_DETAILS.copy(
            exercise = TestData.PUSH_UPS_EXERCISE.copy(
                metadata = ExerciseMetadata(source = ExerciseSource.Bundled)
            )
        )
        exerciseRepository.insert(bundledDetails)

        assertFailsWith<IllegalStateException> {
            service.deleteExercise(bundledDetails.exercise.id)
        }
    }

    @Test
    fun `save exercise with multiple images calls saveImages with all image configurations`() = runTest {
        val imageRef1 = TestData.SAMPLE_IMAGE_REFERENCE
        val imageRef2 = TestData.SAMPLE_IMAGE_REFERENCE.copy(imageId = TestData.IMAGE_ID_2)
        coEvery { imageStorage.saveImages(any()) } returns listOf(imageRef1, imageRef2)

        val multiImageConfig = TestData.PUSH_UPS_CONFIGURATION.copy(
            images = listOf(TestData.SAMPLE_IMAGE_BYTE_ARRAY, TestData.SAMPLE_IMAGE_BYTE_ARRAY)
        )
        val exerciseId = service.saveExercise(exerciseConfiguration = multiImageConfig)

        val imageConfig = TestData.SAMPLE_IMAGE_BYTE_ARRAY.toImageConfiguration(ownerId = exerciseId.value)
        coVerify(exactly = 1) { imageStorage.saveImages(listOf(imageConfig, imageConfig)) }

        val savedExercise = exerciseRepository.exercises.first().exercise
        assertEquals(listOf(imageRef1.imageId, imageRef2.imageId), savedExercise.imageIds)
    }

    @Test
    fun `readImageReferences returns all image references from storage`() = runTest {
        val imageRef1 = TestData.SAMPLE_IMAGE_REFERENCE
        val imageRef2 = TestData.SAMPLE_IMAGE_REFERENCE.copy(imageId = TestData.IMAGE_ID_2)
        coEvery { imageStorage.readImageReferences(TestData.EXERCISE_ID_1.value) } returns listOf(imageRef1, imageRef2)

        val imageReferences = service.readImageReferences(TestData.EXERCISE_ID_1)

        assertEquals(listOf(imageRef1, imageRef2), imageReferences)
    }
}
