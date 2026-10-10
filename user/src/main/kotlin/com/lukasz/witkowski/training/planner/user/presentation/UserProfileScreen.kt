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
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.OrangeContainer
import com.lukasz.witkowski.training.planner.shared.theme.OrangeLight
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControl
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControlItem
import com.lukasz.witkowski.training.planner.user.R
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit

import androidx.compose.runtime.LaunchedEffect

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    UserProfileScreenContent(
        uiState = uiState,
        onWeightUnitChanged = viewModel::onWeightUnitChanged,
        onSignInClick = onSignInClick,
        onSignOutClick = {
            viewModel.signOut()
            onSignOutClick()
        },
        modifier = modifier,
    )
}

@Composable
private fun UserProfileScreenContent(
    uiState: ProfileUiState,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens2.margin),
    ) {
        when (uiState) {
            ProfileUiState.Loading -> {
                AppLoadingState()
            }

            is ProfileUiState.OfflineProfileLoaded,
            is ProfileUiState.OnlineProfileLoaded,
            -> {
                val isOnline = uiState is ProfileUiState.OnlineProfileLoaded
                val failure = (uiState as? ProfileUiState.OfflineProfileLoaded)?.failure
                val guestAthleteText = stringResource(id = R.string.user_profile_guest_athlete)
                val defaultSubtitleText = stringResource(id = R.string.user_profile_local_only_database)
                val username = (uiState as? ProfileUiState.OnlineProfileLoaded)?.username ?: guestAthleteText
                val subtitle = (uiState as? ProfileUiState.OnlineProfileLoaded)?.email ?: defaultSubtitleText
                val weightUnit = when (uiState) {
                    is ProfileUiState.OnlineProfileLoaded -> uiState.weightUnit
                    is ProfileUiState.OfflineProfileLoaded -> uiState.weightUnit
                }

                var showReauthSheet by rememberSaveable(failure) {
                    mutableStateOf(failure != null)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
                ) {
                    UserIdentitySection(
                        isOnline = isOnline,
                        failure = failure,
                        username = username,
                        subtitle = subtitle,
                        onReauthPillClick = { showReauthSheet = true },
                    )

                    if (isOnline) {
                        SignOutCard(
                            onSignOutClick = onSignOutClick,
                        )
                    } else {
                        CloudSyncCtaCard(
                            onSignInClick = onSignInClick,
                        )
                    }

                    OfflinePreferencesCard(
                        selectedWeightUnit = weightUnit,
                        onWeightUnitChanged = onWeightUnitChanged,
                    )

                    EngineFootnote()
                }

                if ((!isOnline) && (failure != null) && showReauthSheet) {
                    ReauthBottomSheet(
                        onSignInClick = {
                            showReauthSheet = false
                            onSignInClick()
                        },
                        onDismissRequest = {
                            showReauthSheet = false
                        },
                        userFailure = failure,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserIdentitySection(
    isOnline: Boolean,
    failure: UserFailure?,
    username: String,
    subtitle: String,
    onReauthPillClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.padding(bottom = Dimens2.spaceSm),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Surface(
                modifier = Modifier.size(Dimens2.avatarSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = Dimens2.shadowLg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.AccountCircle else Icons.Default.NoAccounts,
                        contentDescription = "User Avatar",
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(Dimens2.iconSize42),
                    )
                }
            }

            Surface(
                modifier = Modifier.size(Dimens2.avatarBadgeSize),
                shape = CircleShape,
                color = if (isOnline) {
                    MaterialTheme.colorScheme.primaryContainer
                } else if (failure != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    OrangeContainer
                },
                shadowElevation = Dimens2.shadowSm,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOnline) {
                            Icons.Default.CloudDone
                        } else if (failure != null) {
                            Icons.Default.CloudOff
                        } else {
                            Icons.Default.WifiOff
                        },
                        contentDescription = "Status Badge",
                        tint = if (isOnline) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else if (failure != null) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            OrangeLight
                        },
                        modifier = Modifier.size(Dimens2.iconSize15),
                    )
                }
            }
        }

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            shape = CircleShape,
            color = if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.padding(top = Dimens2.spaceXs),
        ) {
            Text(
                text = stringResource(
                    id = if (isOnline) R.string.user_profile_tag_cloud_synced else R.string.user_profile_tag_local_first,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens2.spaceSm, vertical = Dimens2.spaceXs),
            )
        }

        if ((!isOnline) && (failure != null)) {
            Surface(
                onClick = onReauthPillClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = Dimens2.thinBorder,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                ),
                modifier = Modifier.padding(top = Dimens2.spaceXs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
                    modifier = Modifier.padding(horizontal = Dimens2.spaceGapIconTextMedium, vertical = Dimens2.spaceXs),
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens2.spaceSm),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                    ) {}
                    Text(
                        text = stringResource(id = R.string.user_profile_sync_failed_pill),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens2.spaceXs),
        )
    }
}

@Composable
private fun SignOutCard(
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Surface(
            onClick = onSignOutClick,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens2.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens2.iconSize40),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens2.inputIconSize),
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.user_profile_sign_out_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(id = R.string.user_profile_sign_out_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens2.inputIconSize),
                )
            }
        }
    }
}

@Composable
private fun CloudSyncCtaCard(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(Dimens2.spaceMd)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceGapIconTextMedium),
            ) {
                Surface(
                    modifier = Modifier.size(Dimens2.iconSize40),
                    shape = MaterialTheme.shapes.medium,
                    color = OrangeContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = OrangeLight,
                            modifier = Modifier.size(Dimens2.iconSize22),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.user_profile_cta_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(id = R.string.user_profile_cta_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens2.spaceXs),
                    )
                }
            }

            AppPrimaryButton(
                text = stringResource(id = R.string.user_profile_cta_button),
                onClick = onSignInClick,
                leadingIcon = Icons.AutoMirrored.Filled.Login,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.padding(top = Dimens2.spaceMd),
            )
        }
    }
}

@Composable
private fun OfflinePreferencesCard(
    selectedWeightUnit: WeightUnit,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val kgLabel = stringResource(id = R.string.unit_kg)
    val lbsLabel = stringResource(id = R.string.unit_lbs)

    val items = remember(kgLabel, lbsLabel) {
        listOf(
            AppSegmentedControlItem(value = WeightUnit.KG, label = kgLabel),
            AppSegmentedControlItem(value = WeightUnit.LBS, label = lbsLabel),
        )
    }

    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.user_profile_preferences_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(id = R.string.user_profile_preferences_stored_on_device),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens2.spaceGapIconTextMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimens2.inputIconSize),
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.user_profile_measurement_units_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(id = R.string.user_profile_measurement_units_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }

                    AppSegmentedControl(
                        items = items,
                        selectedItem = selectedWeightUnit,
                        onItemSelected = onWeightUnitChanged,
                        modifier = Modifier.width(Dimens2.iconContainerSize * 2.2f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineFootnote(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens2.spaceSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(Dimens2.iconSize14),
            )
            Text(
                text = stringResource(id = R.string.user_profile_engine_footnote_build),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@AppPreview
@Composable
private fun UserProfileLoadingPreview() {
    TrainingPlannerTheme2 {
        UserProfileScreenContent(
            uiState = ProfileUiState.Loading,
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {},
        )
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOfflinePreview() {
    TrainingPlannerTheme2 {
        UserProfileScreenContent(
            uiState = ProfileUiState.OfflineProfileLoaded(
                weightUnit = WeightUnit.KG,
            ),
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {},
        )
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOfflineFailurePreview() {
    TrainingPlannerTheme2 {
        UserProfileScreenContent(
            uiState = ProfileUiState.OfflineProfileLoaded(
                weightUnit = WeightUnit.KG,
                failure = UserFailure.NetworkError(networkFailure = NetworkFailure.NoInternet),
            ),
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {},
        )
    }
}

@AppPreview
@Composable
private fun UserProfileScreenOnlinePreview() {
    TrainingPlannerTheme2 {
        UserProfileScreenContent(
            uiState = ProfileUiState.OnlineProfileLoaded(
                email = "athlete@example.com",
                username = "John Doe",
                weightUnit = WeightUnit.KG,
            ),
            onWeightUnitChanged = {},
            onSignInClick = {},
            onSignOutClick = {},
        )
    }
}
