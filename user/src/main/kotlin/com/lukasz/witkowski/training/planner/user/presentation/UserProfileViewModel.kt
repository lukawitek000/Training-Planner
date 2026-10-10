package com.lukasz.witkowski.training.planner.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class UserProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val userProfileResult = MutableStateFlow<AppResult<User, UserFailure>?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.weightUnit,
        userProfileResult,
    ) { weightUnit, profileResult ->
        when (profileResult) {
            null -> ProfileUiState.Loading
            is AppResult.Success -> {
                val user = profileResult.value
                Timber.i("User profile state loaded for email: %s, username: %s", user.email, user.username)
                ProfileUiState.OnlineProfileLoaded(
                    email = user.email,
                    username = user.username,
                    weightUnit = weightUnit,
                )
            }
            is AppResult.Error -> {
                when (val failure = profileResult.error) {
                    is UserFailure.NotSignedIn -> {
                        Timber.i("User profile state: Not signed in")
                        ProfileUiState.OfflineProfileLoaded(
                            weightUnit = weightUnit,
                            failure = null,
                        )
                    }
                    else -> {
                        Timber.w("User profile state error: %s", failure)
                        ProfileUiState.OfflineProfileLoaded(
                            weightUnit = weightUnit,
                            failure = failure,
                        )
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState.Loading,
    )

    fun loadUserProfile() {
        Timber.d("Loading user profile...")
        viewModelScope.launch {
            userProfileResult.value = userRepository.getUserProfile()
        }
    }

    fun onWeightUnitChanged(weightUnit: WeightUnit) {
        Timber.i("Changing weight unit to: %s", weightUnit)
        viewModelScope.launch {
            userRepository.setWeightUnit(weightUnit)
        }
    }

    fun signOut() {
        Timber.i("Signing out user from profile screen")
        viewModelScope.launch {
            userRepository.signOut()
            loadUserProfile()
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class OnlineProfileLoaded(
        val email: String,
        val username: String,
        val weightUnit: WeightUnit,
    ) : ProfileUiState
    data class OfflineProfileLoaded(
        val weightUnit: WeightUnit,
        val failure: UserFailure? = null,
    ) : ProfileUiState
}
