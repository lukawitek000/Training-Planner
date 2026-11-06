package com.lukasz.witkowski.training.planner.user.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.LocalFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.OrangeContainer
import com.lukasz.witkowski.training.planner.shared.theme.OrangeLight
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControl
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControlItem
import com.lukasz.witkowski.training.planner.user.R
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    UserProfileScreenContent(
        uiState = uiState,
        onWeightUnitChanged = viewModel::onWeightUnitChanged,
        onSignInClick = onSignInClick,
        onSignOutWithPolicy = { keepLocalData ->
            viewModel.signOut(keepLocalData = keepLocalData)
            onSignOutClick()
        },
        modifier = modifier
    )
}

@Composable
private fun UserProfileScreenContent(
    uiState: ProfileUiState,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit = {},
    onSignOutWithPolicy: (keepLocalData: Boolean) -> Unit = { onSignOutClick() },
    modifier: Modifier = Modifier
) {
    val isOnlineSyncEnabled = LocalFeatureFlagProvider.current.isEnabled(FeatureFlag.ONLINE_SYNC)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is ProfileUiState.Loading -> {
                AppLoadingState()
            }

            is ProfileUiState.OfflineProfileLoaded,
            is ProfileUiState.OnlineProfileLoaded
            -> {
                val isOnline = uiState is ProfileUiState.OnlineProfileLoaded
                val failure = (uiState as? ProfileUiState.OfflineProfileLoaded)?.failure
                val guestAthleteText = stringResource(id = R.string.user_profile_guest_athlete)
                val defaultSubtitleText =
                    stringResource(id = R.string.user_profile_local_only_database)
                val username =
                    (uiState as? ProfileUiState.OnlineProfileLoaded)?.username ?: guestAthleteText
                val subtitle =
                    (uiState as? ProfileUiState.OnlineProfileLoaded)?.email ?: defaultSubtitleText
                val weightUnit =
                    when (uiState) {
                        is ProfileUiState.OnlineProfileLoaded -> uiState.weightUnit
                        is ProfileUiState.OfflineProfileLoaded -> uiState.weightUnit
                    }

                var showReauthSheet by rememberSaveable(failure, isOnlineSyncEnabled) {
                    mutableStateOf(failure != null && isOnlineSyncEnabled)
                }
                var showSignOutSheet by rememberSaveable {
                    mutableStateOf(false)
                }

                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimens.margin)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
                ) {
                    UserIdentitySection(
                        isOnline = isOnline,
                        isOnlineSyncEnabled = isOnlineSyncEnabled,
                        failure = failure,
                        username = username,
                        subtitle = subtitle,
                        onReauthPillClick = { showReauthSheet = true }
                    )

                    if (isOnlineSyncEnabled) {
                        if (isOnline) {
                            SignOutCard(
                                onSignOutClick = { showSignOutSheet = true }
                            )
                        } else {
                            CloudSyncCtaCard(
                                onSignInClick = onSignInClick
                            )
                        }
                    }

                    OfflinePreferencesCard(
                        selectedWeightUnit = weightUnit,
                        onWeightUnitChanged = onWeightUnitChanged
                    )

                    EngineFootnote(appVersion = uiState.appVersion)
                }

                if (isOnlineSyncEnabled && showSignOutSheet) {
                    SignOutBottomSheet(
                        onKeepLocalDataClick = {
                            showSignOutSheet = false
                            onSignOutWithPolicy(true)
                        },
                        onRemoveAllLocalDataClick = {
                            showSignOutSheet = false
                            onSignOutWithPolicy(false)
                        },
                        onDismissRequest = {
                            showSignOutSheet = false
                        }
                    )
                }

                if (isOnlineSyncEnabled && (!isOnline) && (failure != null) && showReauthSheet) {
                    ReauthBottomSheet(
                        onSignInClick = {
                            showReauthSheet = false
                            onSignInClick()
                        },
                        onDismissRequest = {
                            showReauthSheet = false
                        },
                        userFailure = failure
                    )
                }
            }
        }
    }
}

@Composable
private fun UserIdentitySection(
    isOnline: Boolean,
    isOnlineSyncEnabled: Boolean,
    failure: UserFailure?,
    username: String,
    subtitle: String,
    onReauthPillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.padding(bottom = Dimens.spaceSm),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier.size(Dimens.avatarSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = Dimens.shadowLg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector =
                        if (isOnline || !isOnlineSyncEnabled) {
                            Icons.Default.AccountCircle
                        } else {
                            Icons.Default.NoAccounts
                        },
                        contentDescription = "User Avatar",
                        tint =
                        if (isOnline || !isOnlineSyncEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(Dimens.iconSize42)
                    )
                }
            }

            if (isOnlineSyncEnabled) {
                Surface(
                    modifier = Modifier.size(Dimens.avatarBadgeSize),
                    shape = CircleShape,
                    color =
                    if (isOnline) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (failure != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        OrangeContainer
                    },
                    shadowElevation = Dimens.shadowSm
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector =
                            if (isOnline) {
                                Icons.Default.CloudDone
                            } else if (failure != null) {
                                Icons.Default.CloudOff
                            } else {
                                Icons.Default.WifiOff
                            },
                            contentDescription = "Status Badge",
                            tint =
                            if (isOnline) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else if (failure != null) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                OrangeLight
                            },
                            modifier = Modifier.size(Dimens.iconSize15)
                        )
                    }
                }
            }
        }

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = CircleShape,
            color =
            if (isOnline) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            modifier = Modifier.padding(top = Dimens.spaceXs)
        ) {
            Text(
                text =
                stringResource(
                    id =
                    if (isOnline) {
                        R.string.user_profile_tag_cloud_synced
                    } else {
                        R.string.user_profile_tag_local_first
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color =
                if (isOnline) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(
                    horizontal = Dimens.spaceSm,
                    vertical = Dimens.spaceXs
                )
            )
        }

        if (isOnlineSyncEnabled && (!isOnline) && (failure != null)) {
            Surface(
                onClick = onReauthPillClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                border =
                BorderStroke(
                    width = Dimens.thinBorder,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(top = Dimens.spaceXs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                    modifier = Modifier.padding(
                        horizontal = Dimens.spaceGapIconTextMedium,
                        vertical = Dimens.spaceXs
                    )
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens.spaceSm),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error
                    ) {}
                    Text(
                        text = stringResource(id = R.string.user_profile_sync_failed_pill),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.spaceXs)
        )
    }
}

@Composable
private fun SignOutCard(onSignOutClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Surface(
            onClick = onSignOutClick,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens.iconSize40),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.inputIconSize)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.user_profile_sign_out_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(id = R.string.user_profile_sign_out_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.inputIconSize)
                )
            }
        }
    }
}

@Composable
private fun CloudSyncCtaCard(onSignInClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(Dimens.spaceMd)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceGapIconTextMedium)
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.iconSize40),
                    shape = MaterialTheme.shapes.medium,
                    color = OrangeContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = OrangeLight,
                            modifier = Modifier.size(Dimens.iconSize22)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.user_profile_cta_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = R.string.user_profile_cta_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.spaceXs)
                    )
                }
            }

            AppPrimaryButton(
                text = stringResource(id = R.string.user_profile_cta_button),
                onClick = onSignInClick,
                leadingIcon = Icons.AutoMirrored.Filled.Login,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.padding(top = Dimens.spaceMd)
            )
        }
    }
}

@Composable
private fun OfflinePreferencesCard(
    selectedWeightUnit: WeightUnit,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val kgLabel = stringResource(id = R.string.unit_kg)
    val lbsLabel = stringResource(id = R.string.unit_lbs)

    val items =
        remember(kgLabel, lbsLabel) {
            listOf(
                AppSegmentedControlItem(value = WeightUnit.KG, label = kgLabel),
                AppSegmentedControlItem(value = WeightUnit.LBS, label = lbsLabel)
            )
        }

    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.user_profile_preferences_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.user_profile_preferences_stored_on_device),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceGapIconTextMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.inputIconSize)
                        )
                        Column {
                            Text(
                                text = stringResource(
                                    id = R.string.user_profile_measurement_units_title
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.user_profile_measurement_units_subtitle
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    AppSegmentedControl(
                        items = items,
                        selectedItem = selectedWeightUnit,
                        onItemSelected = onWeightUnitChanged,
                        modifier = Modifier.width(Dimens.iconContainerSize * 2.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineFootnote(
    appVersion: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spaceSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(Dimens.iconSize14)
            )
            val footnoteText = if (appVersion.isNotBlank()) {
                stringResource(id = R.string.user_profile_engine_footnote_build, appVersion)
            } else {
                stringResource(id = R.string.user_profile_engine_footnote_default)
            }
            Text(
                text = footnoteText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@AppPreview
@Composable
private fun UserProfileLoadingPreview() {
    TrainingPlannerTheme {
        UserProfileScreenContent(
            uiState = ProfileUiState.Loading(appVersion = "0.1.0-alpha.400"),
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {}
        )
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOfflinePreview() {
    TrainingPlannerTheme {
        UserProfileScreenContent(
            uiState =
            ProfileUiState.OfflineProfileLoaded(
                weightUnit = WeightUnit.KG,
                appVersion = "0.1.0-alpha.400"
            ),
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {}
        )
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOfflineFailurePreview() {
    TrainingPlannerTheme {
        CompositionLocalProvider(
            LocalFeatureFlagProvider provides DefaultFeatureFlagProvider(
                mapOf(FeatureFlag.ONLINE_SYNC to true)
            )
        ) {
            UserProfileScreenContent(
                uiState =
                ProfileUiState.OfflineProfileLoaded(
                    weightUnit = WeightUnit.KG,
                    failure = UserFailure.NetworkError(networkFailure = NetworkFailure.NoInternet),
                    appVersion = "0.1.0-alpha.400"
                ),
                onWeightUnitChanged = {},
                onSignInClick = {},
                onSignOutClick = {}
            )
        }
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOnlinePreview() {
    TrainingPlannerTheme {
        CompositionLocalProvider(
            LocalFeatureFlagProvider provides DefaultFeatureFlagProvider(
                mapOf(FeatureFlag.ONLINE_SYNC to true)
            )
        ) {
            UserProfileScreenContent(
                uiState =
                ProfileUiState.OnlineProfileLoaded(
                    email = "athlete@example.com",
                    username = "John Doe",
                    weightUnit = WeightUnit.KG,
                    appVersion = "0.1.0-alpha.400"
                ),
                onWeightUnitChanged = {},
                onSignInClick = {},
                onSignOutClick = {}
            )
        }
    }
}
