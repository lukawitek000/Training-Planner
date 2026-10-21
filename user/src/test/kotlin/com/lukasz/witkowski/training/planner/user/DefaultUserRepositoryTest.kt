package com.lukasz.witkowski.training.planner.user

import com.lukasz.witkowski.training.planner.network.AccessToken
import com.lukasz.witkowski.training.planner.network.AuthTokens
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.network.TokenStorage
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.user.infrastructure.DefaultUserRepository
import com.lukasz.witkowski.training.planner.user.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("ktlint:standard:max-line-length")
class DefaultUserRepositoryTest {
    private val mockTokenStorage = mockk<TokenStorage>(relaxed = true)
    private val mockUserRemoteDataSource = mockk<UserRemoteDataSource>()
    private val mockUserPreferencesStorage = mockk<UserPreferencesStorage>(relaxed = true)
    private val mockSyncScheduler = mockk<SyncScheduler>(relaxed = true)
    private val timeProvider = TestTimeProvider()

    private lateinit var repository: DefaultUserRepository

    @Before
    fun setUp() {
        every { mockUserPreferencesStorage.weightUnit } returns flowOf(WeightUnit.KG)
        every { mockUserPreferencesStorage.autoSyncEnabled } returns flowOf(true)
        repository =
            DefaultUserRepository(
                tokenStorage = mockTokenStorage,
                userRemoteDataSource = mockUserRemoteDataSource,
                userPreferencesStorage = mockUserPreferencesStorage,
                syncScheduler = mockSyncScheduler
            )
    }

    @Test
    fun `getUserProfile returns NotSignedIn when no refresh token is stored without calling remote data source`() =
        runTest {
            coEvery { mockTokenStorage.getTokens() } returns null

            val result = repository.getUserProfile()

            assertEquals(AppResult.Error(UserFailure.NotSignedIn), result)
            coVerify(exactly = 0) { mockUserRemoteDataSource.getUserProfile() }
        }

    @Test
    fun `getUserProfile fetches profile from remote data source when refresh token is stored`() =
        runTest {
            val tokens =
                AuthTokens(
                    accessToken = AccessToken("valid-access-token", timeProvider.currentInstant()),
                    refreshToken = "valid-refresh-token"
                )
            val expectedUser = User("user-1", "test@example.com", "TestUser")

            coEvery { mockTokenStorage.getTokens() } returns tokens
            coEvery { mockUserRemoteDataSource.getUserProfile() } returns
                AppResult.Success(expectedUser)

            val result = repository.getUserProfile()

            assertEquals(AppResult.Success(expectedUser), result)
            coVerify { mockUserRemoteDataSource.getUserProfile() }
        }

    @Test
    fun `weightUnit emits values from UserPreferencesStorage`() = runTest {
        every { mockUserPreferencesStorage.weightUnit } returns
            flowOf(WeightUnit.KG, WeightUnit.LBS)

        val currentUnit = repository.weightUnit.first()

        assertEquals(WeightUnit.KG, currentUnit)
    }

    @Test
    fun `setWeightUnit calls UserPreferencesStorage setWeightUnit`() = runTest {
        coEvery { mockUserPreferencesStorage.setWeightUnit(WeightUnit.LBS) } returns Unit

        repository.setWeightUnit(WeightUnit.LBS)

        coVerify { mockUserPreferencesStorage.setWeightUnit(WeightUnit.LBS) }
    }

    @Test
    fun `signOut clears tokens and notifies syncScheduler with keepLocalData`() = runTest {
        repository.signOut(keepLocalData = true)

        coVerify { mockTokenStorage.clear() }
        verify { mockSyncScheduler.cancelMutationSync() }
        coVerify { mockSyncScheduler.clearLocalSyncData(keepLocalData = true) }
    }
}
