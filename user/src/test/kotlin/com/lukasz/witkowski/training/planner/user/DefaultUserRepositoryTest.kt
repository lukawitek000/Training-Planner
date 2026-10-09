package com.lukasz.witkowski.training.planner.user

import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.user.infrastructure.DefaultUserRepository
import com.lukasz.witkowski.training.planner.user.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUserRepositoryTest {

    private val mockUserRemoteDataSource = mockk<UserRemoteDataSource>()
    private val mockUserPreferencesStorage = mockk<UserPreferencesStorage>(relaxed = true)

    private lateinit var repository: DefaultUserRepository

    @Before
    fun setUp() {
        every { mockUserPreferencesStorage.weightUnit } returns flowOf(WeightUnit.KG)
        repository = DefaultUserRepository(
            userRemoteDataSource = mockUserRemoteDataSource,
            userPreferencesStorage = mockUserPreferencesStorage,
        )
    }

    @Test
    fun `getUserProfile fetches profile from remote data source`() = runTest {
        val expectedUser = User("user-1", "test@example.com", "TestUser")

        coEvery { mockUserRemoteDataSource.getUserProfile() } returns AppResult.Success(expectedUser)

        val result = repository.getUserProfile()

        assertEquals(AppResult.Success(expectedUser), result)
    }

    @Test
    fun `weightUnit emits values from UserPreferencesStorage`() = runTest {
        every { mockUserPreferencesStorage.weightUnit } returns flowOf(WeightUnit.KG, WeightUnit.LBS)

        val currentUnit = repository.weightUnit.first()

        assertEquals(WeightUnit.KG, currentUnit)
    }

    @Test
    fun `setWeightUnit calls UserPreferencesStorage setWeightUnit`() = runTest {
        coEvery { mockUserPreferencesStorage.setWeightUnit(WeightUnit.LBS) } returns Unit

        repository.setWeightUnit(WeightUnit.LBS)

        coVerify { mockUserPreferencesStorage.setWeightUnit(WeightUnit.LBS) }
    }
}
