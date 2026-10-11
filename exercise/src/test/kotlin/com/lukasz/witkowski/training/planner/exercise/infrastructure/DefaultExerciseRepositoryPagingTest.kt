package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.exercise.TestResourceReader
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExercisePagingConfig
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepositoryConfig
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.network.NetworkConfig
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultExerciseRepositoryPagingTest {
    private val localDataSource = mockk<ExerciseLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<ExerciseRemoteDataSource>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val pagingConfig = ExercisePagingConfig(pageSize = 10, prefetchDistance = 10)
    private val repositoryConfig = ExerciseRepositoryConfig(
        autoSaveRemoteExercises = false,
        pagingConfig = pagingConfig
    )

    private val repository = DefaultExerciseRepository(
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
        config = repositoryConfig,
        ioDispatcher = testDispatcher
    )

    @Test
    fun `queryExercises returns flow of PagingData`() = runTest {
        val query = ExerciseQuery(categories = listOf(ExerciseCategory("Chest")), query = "push")
        val resultFlow: Flow<PagingData<Exercise2>> = repository.queryExercises(query)

        val pagingData = resultFlow.first()
        assertNotNull(pagingData)
    }

    @Test
    fun `test resource reader parses exercises page json successfully`() {
        val jsonString = TestResourceReader.readJson("exercises_page1.json")
        val pagedDto = NetworkConfig.defaultJson.decodeFromString<PagedResponseDto<ExerciseDto>>(jsonString)

        assertNotNull(pagedDto)
        kotlin.test.assertEquals(2, pagedDto.items.size)
        kotlin.test.assertEquals("Push-up", pagedDto.items.first().name)
    }
}
