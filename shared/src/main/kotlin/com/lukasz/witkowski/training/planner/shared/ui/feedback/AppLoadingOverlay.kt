package com.lukasz.witkowski.training.planner.shared.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState

/**
 * Wraps [content] in a [Box] and displays a semi-transparent scrim with [AppLoadingState] when [isLoading] is `true`.
 *
 * Includes `testTag("OverlayContent")` on the root container and `testTag("Loading")` on the loading indicator
 * to preserve compatibility with UI tests.
 */
@Composable
fun AppLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingTitle: String = "Loading...",
    loadingSubtitle: String? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.testTag("OverlayContent")
    ) {
        content()

        if (isLoading) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppLoadingState(
                    title = loadingTitle,
                    subtitle = loadingSubtitle,
                    modifier = Modifier.testTag("Loading")
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun AppLoadingOverlayPreview() {
    TrainingPlannerTheme2 {
        AppLoadingOverlay(
            isLoading = true,
            loadingTitle = "Saving exercise...",
            loadingSubtitle = "Updating your local training catalog."
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Dimens2.spaceLg),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "Background Screen Content",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
