package com.lukasz.witkowski.training.planner.auth.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
}

enum class PasswordStrength {
    EMPTY,
    WEAK,
    MEDIUM,
    STRONG;

    companion object {
        fun compute(password: String): PasswordStrength = when {
            password.isEmpty() -> EMPTY
            password.length < 6 -> WEAK
            password.length < 10 -> MEDIUM
            else -> STRONG
        }
    }
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val signInEmail: String = "",
    val signInPassword: String = "",
    val signUpUsername: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val failure: AuthenticationFailure? = null,
    val isSuccess: Boolean = false,
) {
    val passwordStrength: PasswordStrength
        get() = PasswordStrength.compute(signUpPassword)
}

sealed interface AuthIntent {
    data class ModeChanged(val mode: AuthMode) : AuthIntent
    data class SignInEmailChanged(val email: String) : AuthIntent
    data class SignInPasswordChanged(val password: String) : AuthIntent
    data class SignUpUsernameChanged(val username: String) : AuthIntent
    data class SignUpEmailChanged(val email: String) : AuthIntent
    data class SignUpPasswordChanged(val password: String) : AuthIntent
    data object TogglePasswordVisibility : AuthIntent
    data object DismissFailure : AuthIntent
    data object Submit : AuthIntent
}

class AuthenticationViewModel(
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun processIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.ModeChanged -> handleModeChanged(intent.mode)
            is AuthIntent.SignInEmailChanged -> handleSignInEmailChanged(intent.email)
            is AuthIntent.SignInPasswordChanged -> handleSignInPasswordChanged(intent.password)
            is AuthIntent.SignUpUsernameChanged -> handleSignUpUsernameChanged(intent.username)
            is AuthIntent.SignUpEmailChanged -> handleSignUpEmailChanged(intent.email)
            is AuthIntent.SignUpPasswordChanged -> handleSignUpPasswordChanged(intent.password)
            AuthIntent.TogglePasswordVisibility -> handleTogglePasswordVisibility()
            AuthIntent.DismissFailure -> handleDismissFailure()
            AuthIntent.Submit -> handleSubmit()
        }
    }

    private fun handleModeChanged(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, failure = null) }
    }

    private fun handleSignInEmailChanged(email: String) {
        _uiState.update { it.copy(signInEmail = email, failure = null) }
    }

    private fun handleSignInPasswordChanged(password: String) {
        _uiState.update { it.copy(signInPassword = password, failure = null) }
    }

    private fun handleSignUpUsernameChanged(username: String) {
        _uiState.update { it.copy(signUpUsername = username, failure = null) }
    }

    private fun handleSignUpEmailChanged(email: String) {
        _uiState.update { it.copy(signUpEmail = email, failure = null) }
    }

    private fun handleSignUpPasswordChanged(password: String) {
        _uiState.update { it.copy(signUpPassword = password, failure = null) }
    }

    private fun handleTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun handleDismissFailure() {
        _uiState.update { it.copy(failure = null) }
    }

    private fun handleSubmit() {
        if (_uiState.value.mode == AuthMode.SIGN_IN) {
            signIn()
        } else {
            signUp()
        }
    }

    private fun signIn() {
        val state = _uiState.value
        val form = SignInForm(email = state.signInEmail, password = state.signInPassword)
        if (!form.isValid()) {
            _uiState.update {
                it.copy(
                    failure = if (!form.isValidEmail()) AuthenticationFailure.UserNotFound else AuthenticationFailure.IncorrectPassword,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, failure = null) }
            when (val result = authenticationRepository.signIn(form)) {
                is AuthenticationResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthenticationResult.Failure -> _uiState.update { it.copy(isLoading = false, failure = result.failure) }
            }
        }
    }

    private fun signUp() {
        val state = _uiState.value
        val form = SignUpForm(username = state.signUpUsername, email = state.signUpEmail, password = state.signUpPassword)
        if (!form.isValid()) {
            _uiState.update { it.copy(failure = AuthenticationFailure.UnknownFailure) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, failure = null) }
            when (val result = authenticationRepository.signUp(form)) {
                is AuthenticationResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthenticationResult.Failure -> _uiState.update { it.copy(isLoading = false, failure = result.failure) }
            }
        }
    }
}
