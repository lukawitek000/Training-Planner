package com.lukasz.witkowski.training.planner.auth

import com.lukasz.witkowski.training.planner.auth.domain.model.AccessToken
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.User
import com.lukasz.witkowski.training.planner.auth.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultUserRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.UserRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
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

    private val mockTokenStorage = mockk<TokenStorage>()
    private val mockUserRemoteDataSource = mockk<UserRemoteDataSource>()
    private val mockUserPreferencesStorage = mockk<UserPreferencesStorage>(relaxed = true)
    private val timeProvider = TestTimeProvider()

    private lateinit var repository: DefaultUserRepository

    @Before
    fun setUp() {
        every { mockUserPreferencesStorage.weightUnit } returns flowOf(WeightUnit.KG)
        repository = DefaultUserRepository(
            tokenStorage = mockTokenStorage,
            userRemoteDataSource = mockUserRemoteDataSource,
            userPreferencesStorage = mockUserPreferencesStorage,
        )
    }

    @Test
    fun `getUserProfile returns Unauthorized when token storage has no tokens`() = runTest {
        coEvery { mockTokenStorage.getTokens() } returns null

        val result = repository.getUserProfile()

        assertEquals(AppResult.Error(UserFailure.Unauthorized), result)
    }

    @Test
    fun `getUserProfile fetches profile using access token from token storage`() = runTest {
        val tokens = AuthTokens(
            accessToken = AccessToken("valid-access-token", timeProvider.currentInstant()),
            refreshToken = "refresh-token",
        )
        val expectedUser = User("user-1", "test@example.com", "TestUser")

        coEvery { mockTokenStorage.getTokens() } returns tokens
        coEvery { mockUserRemoteDataSource.getUserProfile("valid-access-token") } returns AppResult.Success(expectedUser)

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
