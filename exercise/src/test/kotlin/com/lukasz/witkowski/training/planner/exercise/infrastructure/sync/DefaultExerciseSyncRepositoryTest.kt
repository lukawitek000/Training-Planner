package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseConflictDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseMutation
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseMutationDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultExerciseSyncRepositoryTest {
    private val exerciseDao = mockk<ExerciseDao>(relaxed = true)
    private val mutationDao = mockk<ExerciseMutationDao>(relaxed = true)
    private val remoteDataSource = mockk<ExerciseRemoteDataSource>(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = DefaultExerciseSyncRepository(
        exerciseDao = exerciseDao,
        mutationDao = mutationDao,
        remoteDataSource = remoteDataSource,
        json = json,
        ioDispatcher = testDispatcher
    )

    @Test
    fun `synchronizeExercises applies change batch and advances sync version`() = runTest {
        coEvery { exerciseDao.getSyncVersion() } returns 5L
        val syncResponse = ExerciseSyncResponseDto(
            changes = emptyList(),
            deletedIds = listOf("ex-deleted"),
            currentVersion = 10L
        )
        coEvery { remoteDataSource.getExerciseChanges(5L) } returns AppResult.Success(syncResponse)

        val result = repository.synchronizeExercises()

        assertTrue(result is AppResult.Success)
        coVerify { exerciseDao.applySyncChanges(any(), listOf("ex-deleted"), 10L) }
    }

    @Test
    fun `processPendingMutations uploads pending mutations and deletes mutation on success`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-1",
            exerciseId = "ex-1",
            idempotencyKey = "key-1",
            expectedVersion = 1L,
            name = "Bench Press",
            description = "Chest",
            categoryNames = "Chest",
            state = "PENDING"
        )
        coEvery { mutationDao.getPendingMutations() } returns listOf(mutation)
        val updatedDto = ExerciseDto(
            id = "ex-1",
            name = "Bench Press",
            description = "Chest",
            categories = emptyList(),
            owner = null,
            source = "USER_CREATED",
            sharing = "PRIVATE",
            revision = 2L,
            recommendations = null
        )
        coEvery { remoteDataSource.updateExercise(any(), any(), any()) } returns AppResult.Success(updatedDto)

        val result = repository.processPendingMutations()

        assertTrue(result is AppResult.Success)
        coVerify { mutationDao.deleteMutation("mut-1") }
    }

    @Test
    fun `processPendingMutations sets CONFLICT state when backend returns conflict failure`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-2",
            exerciseId = "ex-2",
            idempotencyKey = "key-2",
            expectedVersion = 1L,
            name = "Squat",
            description = "Legs",
            categoryNames = "Legs",
            state = "PENDING"
        )
        coEvery { mutationDao.getPendingMutations() } returns listOf(mutation)
        val conflictDto = ExerciseConflictDto(
            message = "Conflict",
            currentServerExercise = ExerciseDto(
                id = "ex-2",
                name = "Server Squat",
                description = "Legs",
                categories = emptyList(),
                owner = null,
                source = "USER_CREATED",
                sharing = "PRIVATE",
                revision = 3L,
                recommendations = null
            )
        )
        coEvery { remoteDataSource.updateExercise(any(), any(), any()) } returns AppResult.Error(ExerciseFailure.Conflict(conflictDto))

        val result = repository.processPendingMutations()

        assertTrue(result is AppResult.Error)
        assertEquals(ExerciseFailure.Conflict(conflictDto), result.error)
        coVerify { mutationDao.updateMutation(match { it.state == "CONFLICT" }) }
    }
}
