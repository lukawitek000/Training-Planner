package com.lukasz.witkowski.training.planner.user.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.user.R
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure

/**
 * Re-authentication Modal Bottom Sheet composable displaying the cloud sync failure warning
 * and providing options to reconnect or remain in offline mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReauthBottomSheet(
    onSignInClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    userFailure: UserFailure = UserFailure.Unauthorized,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        ReauthBottomSheetContent(
            onSignInClick = onSignInClick,
            onDismissRequest = onDismissRequest,
            userFailure = userFailure,
        )
    }
}

@Composable
private fun ReauthBottomSheetContent(
    onSignInClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    userFailure: UserFailure = UserFailure.Unauthorized,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens2.margin)
            .padding(bottom = Dimens2.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(Dimens2.spinnerIconContainerSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
            ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens2.iconSize30),
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceSm))

        Text(
            text = stringResource(id = R.string.reauth_required_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(id = userFailure.toReauthMessageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = Dimens2.maxSubtitleWidth + Dimens2.spaceXl)
                .padding(top = Dimens2.spaceXs),
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceLg))

        AppPrimaryButton(
            text = stringResource(id = R.string.reauth_sign_in_to_reconnect),
            onClick = onSignInClick,
            leadingIcon = Icons.AutoMirrored.Filled.Login,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceSm))

        AppSecondaryButton(
            text = stringResource(id = R.string.reauth_stay_in_offline_mode),
            onClick = onDismissRequest,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@StringRes
internal fun UserFailure.toReauthMessageRes(): Int = when (this) {
    is UserFailure.NotSignedIn,
    is UserFailure.Unauthorized,
    -> R.string.reauth_unauthorized_message
    is UserFailure.UserNotFound -> R.string.reauth_user_not_found_message
    is UserFailure.NetworkError -> R.string.reauth_network_error_message
    is UserFailure.UnknownFailure -> R.string.reauth_unknown_error_message
}

@AppPreview
@Composable
private fun ReauthBottomSheetContentPreview() {
    TrainingPlannerTheme2 {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            ReauthBottomSheetContent(
                onSignInClick = {},
                onDismissRequest = {},
            )
        }
    }
}
