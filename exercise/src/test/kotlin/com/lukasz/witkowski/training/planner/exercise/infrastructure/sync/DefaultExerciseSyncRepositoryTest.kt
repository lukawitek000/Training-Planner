package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseConflictDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExercise
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

    companion object {
        private const val EX_ID_1 = "11111111-1111-1111-1111-111111111111"
        private const val EX_ID_2 = "22222222-2222-2222-2222-222222222222"
        private const val EX_ID_CREATE = "33333333-3333-3333-3333-333333333333"
        private const val EX_ID_DEL = "44444444-4444-4444-4444-444444444444"
    }

    @Test
    fun `synchronizeExercises applies change batch and advances sync version`() = runTest {
        coEvery { exerciseDao.getSyncVersion() } returns 5L
        val syncResponse = ExerciseSyncResponseDto(
            changes = emptyList(),
            deletedIds = listOf(EX_ID_DEL),
            currentVersion = 10L
        )
        coEvery { remoteDataSource.getExerciseChanges(5L) } returns AppResult.Success(syncResponse)

        val result = repository.synchronizeExercises()

        assertTrue(result is AppResult.Success)
        coVerify { exerciseDao.applySyncChanges(any(), listOf(EX_ID_DEL), 10L) }
    }

    @Test
    fun `processPendingMutations uploads pending UPDATE mutations and deletes mutation on success`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-1",
            exerciseId = EX_ID_1,
            type = DbExerciseMutation.TYPE_UPDATE,
            idempotencyKey = "key-1",
            expectedVersion = 1L,
            name = "Bench Press",
            description = "Chest",
            categoryNames = "Chest",
            state = "PENDING"
        )
        coEvery { mutationDao.getPendingMutations() } returns listOf(mutation)
        val updatedDto = ExerciseDto(
            id = EX_ID_1,
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
    fun `processPendingMutations uploads pending CREATE mutations and deletes mutation on success`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-create",
            exerciseId = EX_ID_CREATE,
            type = DbExerciseMutation.TYPE_CREATE,
            idempotencyKey = "key-create",
            expectedVersion = 1L,
            name = "New Exercise",
            description = "Desc",
            categoryNames = "Chest",
            state = "PENDING"
        )
        coEvery { mutationDao.getPendingMutations() } returns listOf(mutation)
        val createdDto = ExerciseDto(
            id = EX_ID_CREATE,
            name = "New Exercise",
            description = "Desc",
            categories = emptyList(),
            owner = null,
            source = "USER_CREATED",
            sharing = "PRIVATE",
            revision = 1L,
            recommendations = null
        )
        coEvery { remoteDataSource.createExercise(any(), "key-create") } returns AppResult.Success(createdDto)

        val result = repository.processPendingMutations()

        assertTrue(result is AppResult.Success)
        coVerify { remoteDataSource.createExercise(any(), "key-create") }
        coVerify { mutationDao.deleteMutation("mut-create") }
    }

    @Test
    fun `processPendingMutations treats 404 on DELETE mutation as success and cascades local deletion`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-del",
            exerciseId = EX_ID_DEL,
            type = DbExerciseMutation.TYPE_DELETE,
            idempotencyKey = "key-del",
            expectedVersion = 1L,
            name = "",
            description = "",
            categoryNames = "",
            state = "PENDING"
        )
        coEvery { mutationDao.getPendingMutations() } returns listOf(mutation)
        coEvery { remoteDataSource.deleteExercise(ExerciseId(EX_ID_DEL)) } returns AppResult.Error(ExerciseFailure.ExerciseNotFound)

        val result = repository.processPendingMutations()

        assertTrue(result is AppResult.Success)
        coVerify { exerciseDao.deleteExerciseAndCascade(EX_ID_DEL) }
        coVerify { mutationDao.deleteMutation("mut-del") }
    }

    @Test
    fun `processPendingMutations sets CONFLICT state when backend returns conflict failure`() = runTest {
        val mutation = DbExerciseMutation(
            id = "mut-2",
            exerciseId = EX_ID_2,
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
                id = EX_ID_2,
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
        coVerify { mutationDao.updateMutation(match { it.state == "CONFLICT" && it.conflictServerJson != null }) }
    }

    @Test
    fun `onUserLogout with keepLocalData false deletes all non-bundled exercises`() = runTest {
        repository.onUserLogout(keepLocalData = false)

        coVerify { mutationDao.deleteAllMutations() }
        coVerify { exerciseDao.deleteSyncVersion() }
        coVerify { exerciseDao.deleteAllNonBundledExercises() }
    }

    @Test
    fun `onUserLogout with keepLocalData true converts non-bundled exercises to LOCAL_ONLY`() = runTest {
        repository.onUserLogout(keepLocalData = true)

        coVerify { mutationDao.deleteAllMutations() }
        coVerify { exerciseDao.deleteSyncVersion() }
        coVerify { exerciseDao.deletePendingDeleteExercises() }
        coVerify { exerciseDao.convertNonBundledExercisesToLocalOnly() }
    }

    @Test
    fun `resolveConflictKeepLocal updates mutation expectedVersion to server revision and resets to PENDING`() = runTest {
        val serverExerciseDto = ExerciseDto(
            id = EX_ID_2,
            name = "Server Squat",
            description = "Legs",
            categories = emptyList(),
            owner = null,
            source = "USER_CREATED",
            sharing = "PRIVATE",
            revision = 4L,
            recommendations = null
        )
        val conflictMutation = DbExerciseMutation(
            id = "mut-2",
            exerciseId = EX_ID_2,
            idempotencyKey = "old-key",
            expectedVersion = 1L,
            name = "Local Squat",
            description = "Legs",
            categoryNames = "Legs",
            state = DbExerciseMutation.STATE_CONFLICT,
            conflictServerJson = json.encodeToString(serverExerciseDto)
        )
        coEvery { mutationDao.getMutationByExerciseIdOnce(EX_ID_2) } returns conflictMutation

        val result = repository.resolveConflictKeepLocal(ExerciseId(EX_ID_2))

        assertTrue(result is AppResult.Success)
        coVerify {
            mutationDao.updateMutation(
                match {
                    it.state == DbExerciseMutation.STATE_PENDING &&
                        it.expectedVersion == 4L &&
                        it.conflictServerJson == null
                }
            )
        }
        coVerify { exerciseDao.updateSyncState(EX_ID_2, DbExercise.SYNC_STATE_PENDING_UPLOAD) }
    }
}
