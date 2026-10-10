package com.lukasz.witkowski.training.planner.user.presentation

import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val weightUnitFlow = MutableStateFlow(WeightUnit.KG)

    private lateinit var viewModel: UserProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.weightUnit } returns weightUnitFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when profile loading succeeds, uiState emits OnlineProfileLoaded with weightUnit`() = runTest {
        val user = User("1", "test@example.com", "testuser")
        coEvery { userRepository.getUserProfile() } returns AppResult.Success(user)

        viewModel = UserProfileViewModel(userRepository)

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OnlineProfileLoaded(
                    email = "test@example.com",
                    username = "testuser",
                    weightUnit = WeightUnit.KG,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when profile loading fails with NotSignedIn, uiState emits OfflineProfileLoaded with null failure`() = runTest {
        coEvery { userRepository.getUserProfile() } returns AppResult.Error(UserFailure.NotSignedIn)

        viewModel = UserProfileViewModel(userRepository)

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OfflineProfileLoaded(
                    weightUnit = WeightUnit.KG,
                    failure = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when profile loading fails with Unauthorized, uiState emits OfflineProfileLoaded with failure`() = runTest {
        coEvery { userRepository.getUserProfile() } returns AppResult.Error(UserFailure.Unauthorized)

        viewModel = UserProfileViewModel(userRepository)

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OfflineProfileLoaded(
                    weightUnit = WeightUnit.KG,
                    failure = UserFailure.Unauthorized,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when profile loading fails with NetworkError, uiState emits OfflineProfileLoaded with failure`() = runTest {
        coEvery { userRepository.getUserProfile() } returns AppResult.Error(
            UserFailure.NetworkError(NetworkFailure.NoInternet),
        )

        viewModel = UserProfileViewModel(userRepository)

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OfflineProfileLoaded(
                    weightUnit = WeightUnit.KG,
                    failure = UserFailure.NetworkError(NetworkFailure.NoInternet),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when weight unit changes in repository, uiState updates with new weight unit`() = runTest {
        val user = User("1", "test@example.com", "testuser")
        coEvery { userRepository.getUserProfile() } returns AppResult.Success(user)

        viewModel = UserProfileViewModel(userRepository)

        viewModel.uiState.test {
            assertEquals(
                ProfileUiState.OnlineProfileLoaded(
                    email = "test@example.com",
                    username = "testuser",
                    weightUnit = WeightUnit.KG,
                ),
                awaitItem(),
            )

            weightUnitFlow.value = WeightUnit.LBS

            assertEquals(
                ProfileUiState.OnlineProfileLoaded(
                    email = "test@example.com",
                    username = "testuser",
                    weightUnit = WeightUnit.LBS,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `onWeightUnitChanged calls setWeightUnit on repository`() = runTest {
        coEvery { userRepository.getUserProfile() } returns AppResult.Error(UserFailure.Unauthorized)

        viewModel = UserProfileViewModel(userRepository)
        viewModel.onWeightUnitChanged(WeightUnit.LBS)

        coVerify { userRepository.setWeightUnit(WeightUnit.LBS) }
    }
}
