package com.lukasz.witkowski.training.planner.image.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.lukasz.witkowski.training.planner.image.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2

internal fun calculatePreviousCarouselIndex(
    currentIndex: Int,
    totalCount: Int,
    cycle: Boolean = true,
): Int {
    if (totalCount <= 0) return 0
    return if (currentIndex > 0) {
        currentIndex - 1
    } else if (cycle) {
        totalCount - 1
    } else {
        0
    }
}

internal fun calculateNextCarouselIndex(
    currentIndex: Int,
    totalCount: Int,
    cycle: Boolean = true,
): Int {
    if (totalCount <= 0) return 0
    return if (currentIndex < totalCount - 1) {
        currentIndex + 1
    } else if (cycle) {
        0
    } else {
        totalCount - 1
    }
}

@Composable
fun PhotoCountBadge(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    if (totalCount <= 1) return
    Surface(
        shape = RoundedCornerShape(Dimens2.inputIconSize),
        color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.75f),
        border = BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens2.badgeHorizontalPadding,
                vertical = Dimens2.badgeVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens2.iconSize14)
            )
            Text(
                text = stringResource(R.string.image_counter_format, currentIndex + 1, totalCount),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CarouselNavButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = Dimens2.spaceXl,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.75f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        shadowElevation = Dimens2.shadowSm,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Dimens2.iconSize18),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
fun BoxScope.CarouselNavigationArrows(
    currentIndex: Int,
    totalCount: Int,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cycle: Boolean = true,
) {
    if (totalCount <= 1) return

    val canGoPrev = cycle || currentIndex > 0
    val canGoNext = cycle || currentIndex < totalCount - 1

    CarouselNavigationArrows(
        onPreviousClicked = {
            onIndexChanged(calculatePreviousCarouselIndex(currentIndex, totalCount, cycle))
        },
        onNextClicked = {
            onIndexChanged(calculateNextCarouselIndex(currentIndex, totalCount, cycle))
        },
        isPreviousEnabled = canGoPrev,
        isNextEnabled = canGoNext,
        modifier = modifier
    )
}

@Composable
fun BoxScope.CarouselNavigationArrows(
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isPreviousEnabled: Boolean = true,
    isNextEnabled: Boolean = true,
) {
    CarouselNavButton(
        onClick = onPreviousClicked,
        contentDescription = stringResource(R.string.previous_image),
        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        enabled = isPreviousEnabled,
        modifier = modifier
            .align(Alignment.CenterStart)
            .padding(start = Dimens2.spaceSm)
            .testTag("carousel_prev_button")
    )

    CarouselNavButton(
        onClick = onNextClicked,
        contentDescription = stringResource(R.string.next_image),
        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        enabled = isNextEnabled,
        modifier = modifier
            .align(Alignment.CenterEnd)
            .padding(end = Dimens2.spaceSm)
            .testTag("carousel_next_button")
    )
}
