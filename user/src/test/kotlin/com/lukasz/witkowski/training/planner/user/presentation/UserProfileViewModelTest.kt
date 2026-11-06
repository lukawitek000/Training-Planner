package com.lukasz.witkowski.training.planner.user.presentation

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.AppVersionProvider
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("ktlint:standard:max-line-length")
class UserProfileViewModelTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val appVersionProvider: AppVersionProvider = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val weightUnitFlow = MutableStateFlow(WeightUnit.KG)

    private val testVersionName = "0.1.0-test"

    private lateinit var viewModel: UserProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.weightUnit } returns weightUnitFlow
        every { appVersionProvider.versionName } returns testVersionName
        every { appVersionProvider.versionCode } returns 10400
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Loading with appVersion`() =
        runTest {
            viewModel = UserProfileViewModel(userRepository, appVersionProvider)
            assertEquals(ProfileUiState.Loading(testVersionName), viewModel.uiState.value)
        }

    @Test
    fun `when profile loading succeeds, uiState emits OnlineProfileLoaded with weightUnit and appVersion`() =
        runTest {
            val user = User("1", "test@example.com", "testuser")
            coEvery { userRepository.getUserProfile() } returns AppResult.Success(user)

            viewModel = UserProfileViewModel(userRepository, appVersionProvider)
            viewModel.loadUserProfile()

            viewModel.uiState.test {
                assertEquals(
                    ProfileUiState.OnlineProfileLoaded(
                        email = "test@example.com",
                        username = "testuser",
                        weightUnit = WeightUnit.KG,
                        appVersion = testVersionName
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when profile loading fails with NotSignedIn, uiState emits OfflineProfileLoaded with null failure and appVersion`() =
        runTest {
            coEvery { userRepository.getUserProfile() } returns
                AppResult.Error(UserFailure.NotSignedIn)

            viewModel = UserProfileViewModel(userRepository, appVersionProvider)
            viewModel.loadUserProfile()

            viewModel.uiState.test {
                assertEquals(
                    ProfileUiState.OfflineProfileLoaded(
                        weightUnit = WeightUnit.KG,
                        failure = null,
                        appVersion = testVersionName
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when profile loading fails with Unauthorized, uiState emits OfflineProfileLoaded with failure and appVersion`() =
        runTest {
            coEvery { userRepository.getUserProfile() } returns
                AppResult.Error(UserFailure.Unauthorized)

            viewModel = UserProfileViewModel(userRepository, appVersionProvider)
            viewModel.loadUserProfile()

            viewModel.uiState.test {
                assertEquals(
                    ProfileUiState.OfflineProfileLoaded(
                        weightUnit = WeightUnit.KG,
                        failure = UserFailure.Unauthorized,
                        appVersion = testVersionName
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when profile loading fails with NetworkError, uiState emits OfflineProfileLoaded with failure and appVersion`() =
        runTest {
            coEvery { userRepository.getUserProfile() } returns
                AppResult.Error(
                    UserFailure.NetworkError(NetworkFailure.NoInternet)
                )

            viewModel = UserProfileViewModel(userRepository, appVersionProvider)
            viewModel.loadUserProfile()

            viewModel.uiState.test {
                assertEquals(
                    ProfileUiState.OfflineProfileLoaded(
                        weightUnit = WeightUnit.KG,
                        failure = UserFailure.NetworkError(NetworkFailure.NoInternet),
                        appVersion = testVersionName
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when weight unit changes in repository, uiState updates with new weight unit`() = runTest {
        val user = User("1", "test@example.com", "testuser")
        coEvery { userRepository.getUserProfile() } returns AppResult.Success(user)

        viewModel = UserProfileViewModel(userRepository, appVersionProvider)
        viewModel.loadUserProfile()

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OnlineProfileLoaded(
                    email = "test@example.com",
                    username = "testuser",
                    weightUnit = WeightUnit.KG,
                    appVersion = testVersionName
                ),
                awaitItem()
            )

            weightUnitFlow.value = WeightUnit.LBS

            assertEquals(
                ProfileUiState.OnlineProfileLoaded(
                    email = "test@example.com",
                    username = "testuser",
                    weightUnit = WeightUnit.LBS,
                    appVersion = testVersionName
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `onWeightUnitChanged calls setWeightUnit on repository`() = runTest {
        coEvery { userRepository.getUserProfile() } returns
            AppResult.Error(UserFailure.Unauthorized)

        viewModel = UserProfileViewModel(userRepository, appVersionProvider)
        viewModel.onWeightUnitChanged(WeightUnit.LBS)

        coVerify { userRepository.setWeightUnit(WeightUnit.LBS) }
    }

    @Test
    fun `signOut calls repository with keepLocalData and reloads profile`() = runTest {
        val user = User("1", "test@example.com", "testuser")
        coEvery { userRepository.getUserProfile() } returns AppResult.Success(user)

        viewModel = UserProfileViewModel(userRepository, appVersionProvider)
        viewModel.loadUserProfile()

        coEvery { userRepository.getUserProfile() } returns AppResult.Error(UserFailure.NotSignedIn)
        viewModel.signOut(keepLocalData = true)

        coVerify { userRepository.signOut(keepLocalData = true) }
        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OfflineProfileLoaded(
                    weightUnit = WeightUnit.KG,
                    failure = null,
                    appVersion = testVersionName
                ),
                awaitItem()
            )
        }
    }
}
