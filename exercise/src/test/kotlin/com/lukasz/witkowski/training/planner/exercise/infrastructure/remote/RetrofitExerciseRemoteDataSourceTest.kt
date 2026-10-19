package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.OwnerDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.UnknownHostException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitExerciseRemoteDataSourceTest {
    private val api = mockk<ExerciseApi>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dataSource = RetrofitExerciseRemoteDataSource(
        api = api,
        ioDispatcher = testDispatcher
    )

    @Test
    fun `getCategories returns success with categories when API succeeds`() = runTest {
        val categoryDtos = listOf(
            CategoryDto(id = "c1", name = "Chest"),
            CategoryDto(id = "c2", name = "Back")
        )
        coEvery { api.getCategories() } returns categoryDtos

        val result = dataSource.getCategories()

        assertTrue(result is AppResult.Success)
        assertEquals(
            listOf(ExerciseCategory("Chest"), ExerciseCategory("Back")),
            result.value
        )
    }

    @Test
    fun `getExercises returns mapped paged response when API succeeds`() = runTest {
        val exerciseDto = ExerciseDto(
            id = UUID.randomUUID().toString(),
            name = "Push-ups",
            description = "Chest exercise",
            categories = listOf(CategoryDto("c1", "Chest")),
            owner = OwnerDto(id = "user-123", username = "JohnDoe"),
            source = "BUNDLED",
            sharing = "PRIVATE",
            revision = 1L,
            recommendations = null
        )
        val pagedDto = PagedResponseDto(
            items = listOf(exerciseDto),
            page = 1,
            limit = 10,
            totalItems = 1,
            totalPages = 1
        )
        coEvery { api.getExercises(any(), any(), any(), any()) } returns pagedDto

        val result = dataSource.getExercises(currentUserId = "user-123")

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.items.size)
        val exercise = result.value.items.first()
        assertEquals("Push-ups", exercise.name)
        assertEquals(ExerciseSource.Bundled, exercise.metadata.source)
    }

    @Test
    fun `getExerciseById returns Exercise2 when API succeeds`() = runTest {
        val exerciseId = ExerciseId.create()
        val exerciseDto = ExerciseDto(
            id = exerciseId.toString(),
            name = "Squats",
            description = "Leg exercise",
            categories = listOf(CategoryDto("c2", "Legs")),
            owner = null,
            source = "BUNDLED",
            sharing = "PRIVATE",
            revision = 1L,
            recommendations = null
        )
        coEvery { api.getExerciseById(exerciseId.toString()) } returns exerciseDto

        val result = dataSource.getExerciseById(exerciseId)

        assertTrue(result is AppResult.Success)
        assertEquals("Squats", result.value.name)
        assertEquals(ExerciseSource.Bundled, result.value.metadata.source)
    }

    @Test
    fun `createExercise returns ExerciseDto when API succeeds`() = runTest {
        val exerciseId = ExerciseId.create()
        val dummyRec = ExerciseRecommendation(0, 0, 0.seconds, null)
        val domainExercise = Exercise2(
            id = exerciseId,
            name = "Lunges",
            description = "Legs exercise",
            categories = setOf(ExerciseCategory("Legs")),
            metadata = ExerciseMetadata(source = ExerciseSource.UserCreated(ExerciseOwner.User("user-123", "JohnDoe")))
        )
        val exerciseDetails = ExerciseDetails(
            exercise = domainExercise,
            beginnerRecommendation = dummyRec,
            intermediateRecommendation = dummyRec,
            advancedRecommendation = dummyRec
        )
        val returnedDto = ExerciseDto(
            id = exerciseId.toString(),
            name = "Lunges",
            description = "Legs exercise",
            categories = listOf(CategoryDto("c2", "Legs")),
            owner = OwnerDto("user-123", "JohnDoe"),
            source = "USER_CREATED",
            sharing = "PRIVATE",
            revision = 1L,
            recommendations = null
        )
        coEvery { api.createExercise(any()) } returns returnedDto

        val result = dataSource.createExercise(exerciseDetails, idempotencyKey = "idem-1")

        assertTrue(result is AppResult.Success)
        assertEquals("Lunges", result.value.name)
        coVerify {
            api.createExercise(
                match {
                    it.id == exerciseId.toString() &&
                        it.name == "Lunges" &&
                        it.idempotencyKey == "idem-1"
                }
            )
        }
    }

    @Test
    fun `updateExercise returns ExerciseDto when API succeeds`() = runTest {
        val exerciseId = ExerciseId.create()
        val dummyRec = ExerciseRecommendation(0, 0, 0.seconds, null)
        val domainExercise = Exercise2(
            id = exerciseId,
            name = "Updated Lunges",
            description = "Legs exercise updated",
            categories = setOf(ExerciseCategory("Legs")),
            metadata = ExerciseMetadata(source = ExerciseSource.UserCreated(ExerciseOwner.User("user-123", "JohnDoe")))
        )
        val exerciseDetails = ExerciseDetails(
            exercise = domainExercise,
            beginnerRecommendation = dummyRec,
            intermediateRecommendation = dummyRec,
            advancedRecommendation = dummyRec
        )
        val returnedDto = ExerciseDto(
            id = exerciseId.toString(),
            name = "Updated Lunges",
            description = "Legs exercise updated",
            categories = listOf(CategoryDto("c2", "Legs")),
            owner = OwnerDto("user-123", "JohnDoe"),
            source = "USER_CREATED",
            sharing = "PRIVATE",
            revision = 1L,
            recommendations = null
        )
        coEvery { api.updateExercise(exerciseId.toString(), any()) } returns returnedDto

        val result = dataSource.updateExercise(exerciseDetails, expectedVersion = 1L, idempotencyKey = "idem-2")

        assertTrue(result is AppResult.Success)
        assertEquals("Updated Lunges", result.value.name)
        coVerify { api.updateExercise(exerciseId.toString(), any()) }
    }

    @Test
    fun `deleteExercise returns Success when API succeeds`() = runTest {
        val exerciseId = ExerciseId.create()
        coEvery { api.deleteExercise(exerciseId.toString()) } returns Response.success(Unit)

        val result = dataSource.deleteExercise(exerciseId)

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `getExerciseById maps 404 to ExerciseNotFound`() = runTest {
        val exerciseId = ExerciseId.create()
        val errorResponseBody = "{\"message\":\"Not found\",\"errorCode\":\"NOT_FOUND\"}"
            .toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<ExerciseDto>(404, errorResponseBody))

        coEvery { api.getExerciseById(exerciseId.toString()) } throws httpException

        val result = dataSource.getExerciseById(exerciseId)

        assertEquals(AppResult.Error(ExerciseFailure.ExerciseNotFound), result)
    }

    @Test
    fun `getExerciseById maps 401 to Unauthorized`() = runTest {
        val exerciseId = ExerciseId.create()
        val errorResponseBody = "{\"message\":\"Unauthorized\"}"
            .toResponseBody("application/json".toMediaType())
        val httpException = HttpException(Response.error<ExerciseDto>(401, errorResponseBody))

        coEvery { api.getExerciseById(exerciseId.toString()) } throws httpException

        val result = dataSource.getExerciseById(exerciseId)

        assertEquals(AppResult.Error(ExerciseFailure.Unauthorized), result)
    }

    @Test
    fun `getExerciseById maps UnknownHostException to NoInternet`() = runTest {
        val exerciseId = ExerciseId.create()
        coEvery { api.getExerciseById(exerciseId.toString()) } throws UnknownHostException("No host")

        val result = dataSource.getExerciseById(exerciseId)

        assertEquals(
            AppResult.Error(ExerciseFailure.NetworkError(NetworkFailure.NoInternet)),
            result
        )
    }

    @Test
    fun `getExerciseById rethrows CancellationException`() = runTest {
        val exerciseId = ExerciseId.create()
        coEvery { api.getExerciseById(exerciseId.toString()) } throws CancellationException("Cancelled")

        assertFailsWith<CancellationException> {
            dataSource.getExerciseById(exerciseId)
        }
    }
}
