package com.lukasz.witkowski.training.planner.auth.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import com.lukasz.witkowski.training.planner.auth.R
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.appIcon.AppIconBox
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppTertiaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppLabeledTextField
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControl
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControlItem
import kotlin.time.Duration.Companion.seconds

@Composable
fun AuthenticationScreen(
    viewModel: AuthenticationViewModel,
    modifier: Modifier = Modifier,
    onContinueAsGuestClick: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onAuthSuccess()
        }
    }

    AuthenticationScreenContent(
        uiState = uiState,
        onIntent = viewModel::processIntent,
        onContinueAsGuestClick = onContinueAsGuestClick,
        modifier = modifier,
    )
}

@Composable
private fun AuthenticationScreenContent(
    uiState: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
    onContinueAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens2.margin),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceLg),
        ) {
            AuthHeroHeader(mode = uiState.mode)

            AuthSegmentedTab(
                mode = uiState.mode,
                onModeChanged = { onIntent(AuthIntent.ModeChanged(it)) },
            )

            if (uiState.failure != null) {
                AuthFailureBanner(
                    failure = uiState.failure,
                    onDismiss = { onIntent(AuthIntent.DismissFailure) },
                )
            }

            if (uiState.isLoading) {
                AuthLoadingBanner()
            }

            // Forms
            if (uiState.mode == AuthMode.SIGN_IN) {
                SignInFormContent(
                    email = uiState.signInEmail,
                    password = uiState.signInPassword,
                    isPasswordVisible = uiState.isPasswordVisible,
                    isLoading = uiState.isLoading,
                    onEmailChanged = { onIntent(AuthIntent.SignInEmailChanged(it)) },
                    onPasswordChanged = { onIntent(AuthIntent.SignInPasswordChanged(it)) },
                    onTogglePasswordVisibility = { onIntent(AuthIntent.TogglePasswordVisibility) },
                    onSubmit = { onIntent(AuthIntent.Submit) },
                )
            } else {
                SignUpFormContent(
                    username = uiState.signUpUsername,
                    email = uiState.signUpEmail,
                    password = uiState.signUpPassword,
                    passwordStrength = uiState.passwordStrength,
                    isPasswordVisible = uiState.isPasswordVisible,
                    isLoading = uiState.isLoading,
                    onUsernameChanged = { onIntent(AuthIntent.SignUpUsernameChanged(it)) },
                    onEmailChanged = { onIntent(AuthIntent.SignUpEmailChanged(it)) },
                    onPasswordChanged = { onIntent(AuthIntent.SignUpPasswordChanged(it)) },
                    onTogglePasswordVisibility = { onIntent(AuthIntent.TogglePasswordVisibility) },
                    onSubmit = { onIntent(AuthIntent.Submit) },
                )
            }

            // Or Continue With Divider & Guest Action
            OrContinueWithDivider()

            AppTertiaryButton(
                text = stringResource(id = R.string.auth_continue_as_guest),
                onClick = onContinueAsGuestClick,
                leadingIcon = Icons.Default.CloudOff,
                modifier = Modifier.fillMaxWidth(),
            )

            // Bottom Prompt Toggle
            TextButton(
                onClick = {
                    val newMode = if (uiState.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
                    onIntent(AuthIntent.ModeChanged(newMode))
                },
            ) {
                val promptText = if (uiState.mode == AuthMode.SIGN_IN) {
                    "${stringResource(id = R.string.auth_dont_have_account_prompt)} ${stringResource(id = R.string.auth_sign_up_tab)}"
                } else {
                    "${stringResource(id = R.string.auth_already_have_account_prompt)} ${stringResource(id = R.string.auth_sign_in_tab)}"
                }
                Text(
                    text = promptText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AuthHeroHeader(
    mode: AuthMode,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
    ) {
        AppIconBox(
            mainIcon = Icons.Default.FitnessCenter,
            isPremium = true,
            badgeIcon = Icons.Default.Bolt,
        )

        val headingText = if (mode == AuthMode.SIGN_IN) {
            stringResource(id = R.string.auth_welcome_back_heading)
        } else {
            stringResource(id = R.string.auth_create_account_heading)
        }

        val subtextText = if (mode == AuthMode.SIGN_IN) {
            stringResource(id = R.string.auth_welcome_back_subtext)
        } else {
            stringResource(id = R.string.auth_create_account_subtext)
        }

        Text(
            text = headingText,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = subtextText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AuthSegmentedTab(
    mode: AuthMode,
    onModeChanged: (AuthMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val signInLabel = stringResource(id = R.string.auth_sign_in_tab)
    val signUpLabel = stringResource(id = R.string.auth_sign_up_tab)

    val items = remember(signInLabel, signUpLabel) {
        listOf(
            AppSegmentedControlItem(
                value = AuthMode.SIGN_IN,
                label = signInLabel,
                icon = Icons.AutoMirrored.Filled.Login,
            ),
            AppSegmentedControlItem(
                value = AuthMode.SIGN_UP,
                label = signUpLabel,
                icon = Icons.Default.PersonAdd,
            ),
        )
    }

    AppSegmentedControl(
        items = items,
        selectedItem = mode,
        onItemSelected = onModeChanged,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun AuthFailureBanner(
    failure: AuthenticationFailure,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messageText = when (failure) {
        is AuthenticationFailure.UserNotFound -> stringResource(id = R.string.auth_failure_user_not_found)
        is AuthenticationFailure.IncorrectPassword -> stringResource(id = R.string.auth_failure_incorrect_password)
        is AuthenticationFailure.UserAlreadyExists -> stringResource(id = R.string.auth_failure_user_already_exists)
        is AuthenticationFailure.NetworkError -> stringResource(id = R.string.auth_failure_network_error)
        else -> stringResource(id = R.string.auth_failure_unknown)
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceMd),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
        ) {
            Surface(
                modifier = Modifier.size(Dimens2.iconSize30),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(Dimens2.inputIconSize),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.auth_failure_banner_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(top = Dimens2.spaceXs),
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(Dimens2.iconSize24),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(Dimens2.iconSize16),
                )
            }
        }
    }
}

@Composable
private fun AuthLoadingBanner(
    modifier: Modifier = Modifier,
) {
    var isTakingLonger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2.seconds)
        isTakingLonger = true
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens2.inputIconSize),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = Dimens2.thinBorder,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.auth_loading_in_progress),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                AnimatedVisibility(visible = isTakingLonger) {
                    Text(
                        text = stringResource(id = R.string.auth_loading_taking_longer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens2.spaceXs),
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInFormContent(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
    ) {
        AppLabeledTextField(
            label = stringResource(id = R.string.auth_email_label),
            value = email,
            onValueChange = onEmailChanged,
            placeholder = stringResource(id = R.string.auth_email_placeholder),
            leadingIcon = Icons.Default.Mail,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        AppLabeledTextField(
            label = stringResource(id = R.string.auth_password_label),
            value = password,
            onValueChange = onPasswordChanged,
            placeholder = stringResource(id = R.string.auth_password_placeholder),
            leadingIcon = Icons.Default.Lock,
            enabled = !isLoading,
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    enabled = !isLoading,
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(Dimens2.inputIconSize),
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
        )

        val buttonText = if (isLoading) {
            stringResource(id = R.string.auth_signing_in_loading)
        } else {
            stringResource(id = R.string.auth_sign_in_button)
        }

        val trailingIconComposable: @Composable () -> Unit = if (isLoading) {
            @Composable {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens2.inputIconSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Dimens2.thinBorder,
                )
            }
        } else {
            @Composable {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens2.inputIconSize),
                )
            }
        }

        AppPrimaryButton(
            text = buttonText,
            onClick = onSubmit,
            enabled = !isLoading,
            trailingIcon = trailingIconComposable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SignUpFormContent(
    username: String,
    email: String,
    password: String,
    passwordStrength: PasswordStrength,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
    ) {
        AppLabeledTextField(
            label = stringResource(id = R.string.auth_username_label),
            value = username,
            onValueChange = onUsernameChanged,
            placeholder = stringResource(id = R.string.auth_username_placeholder),
            leadingIcon = Icons.Default.Person,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        AppLabeledTextField(
            label = stringResource(id = R.string.auth_email_label),
            value = email,
            onValueChange = onEmailChanged,
            placeholder = stringResource(id = R.string.auth_email_placeholder),
            leadingIcon = Icons.Default.Mail,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)) {
            AppLabeledTextField(
                label = stringResource(id = R.string.auth_password_label),
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = stringResource(id = R.string.auth_password_placeholder),
                leadingIcon = Icons.Default.Lock,
                enabled = !isLoading,
                trailingIcon = {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        enabled = !isLoading,
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(Dimens2.inputIconSize),
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            if (password.isNotEmpty()) {
                PasswordStrengthGauge(strength = passwordStrength)
            }
        }

        val buttonText = if (isLoading) {
            stringResource(id = R.string.auth_signing_up_loading)
        } else {
            stringResource(id = R.string.auth_create_account_button)
        }

        val trailingIconComposable: @Composable () -> Unit = if (isLoading) {
            @Composable {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens2.inputIconSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Dimens2.thinBorder,
                )
            }
        } else {
            @Composable {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens2.inputIconSize),
                )
            }
        }

        AppPrimaryButton(
            text = buttonText,
            onClick = onSubmit,
            enabled = !isLoading,
            trailingIcon = trailingIconComposable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PasswordStrengthGauge(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val (labelRes, activeSegments, activeColor) = when (strength) {
        PasswordStrength.EMPTY, PasswordStrength.WEAK -> Triple(
            R.string.auth_password_weak,
            1,
            MaterialTheme.colorScheme.error,
        )
        PasswordStrength.MEDIUM -> Triple(
            R.string.auth_password_medium,
            2,
            Warning,
        )
        PasswordStrength.STRONG -> Triple(
            R.string.auth_password_strong,
            3,
            Success,
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
    ) {
        for (i in 1..3) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens2.spaceXs),
                shape = MaterialTheme.shapes.small,
                color = if (i <= activeSegments) activeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {}
        }
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = activeColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun OrContinueWithDivider(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            thickness = Dimens2.thinBorder,
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = Dimens2.spaceSm),
        ) {
            Text(
                text = stringResource(id = R.string.auth_or_continue_with).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@AppPreview
@Composable
private fun AuthenticationSignInPreview() {
    TrainingPlannerTheme2 {
        AuthenticationScreenContent(
            uiState = AuthUiState(
                mode = AuthMode.SIGN_IN,
                signInEmail = "athlete@ironlog.io",
            ),
            onIntent = {},
            onContinueAsGuestClick = {},
        )
    }
}

@AppPreview
@Composable
private fun AuthenticationSignUpPreview() {
    TrainingPlannerTheme2 {
        AuthenticationScreenContent(
            uiState = AuthUiState(
                mode = AuthMode.SIGN_UP,
                signUpUsername = "alex_iron",
                signUpEmail = "athlete@trainingplanner.app",
                signUpPassword = "SuperSecret123!",
            ),
            onIntent = {},
            onContinueAsGuestClick = {},
        )
    }
}

@AppPreview
@Composable
private fun AuthenticationFailurePreview() {
    TrainingPlannerTheme2 {
        AuthenticationScreenContent(
            uiState = AuthUiState(
                mode = AuthMode.SIGN_IN,
                signInEmail = "alex.athlete@trainingplanner.app",
                failure = AuthenticationFailure.IncorrectPassword,
            ),
            onIntent = {},
            onContinueAsGuestClick = {},
        )
    }
}

@AppPreview
@Composable
private fun AuthenticationLoadingPreview() {
    TrainingPlannerTheme2 {
        AuthenticationScreenContent(
            uiState = AuthUiState(
                mode = AuthMode.SIGN_IN,
                signInEmail = "alex.athlete@trainingplanner.app",
                isLoading = true,
            ),
            onIntent = {},
            onContinueAsGuestClick = {},
        )
    }
}
