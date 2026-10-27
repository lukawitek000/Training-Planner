package com.lukasz.witkowski.training.planner.image.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import java.io.File

@Composable
fun ImageGallery(
    images: List<ImageReference>,
    modifier: Modifier = Modifier,
    heroHeight: Dp = Dimens2.heroImageHeightStandard,
    thumbnailSize: Dp = Dimens2.mediaThumbnailSize,
) {
    if (images.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    val activeIndex = selectedIndex.coerceIn(images.indices)
    val activeImage = images[activeIndex]
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (images.size > 1) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
                .clip(RoundedCornerShape(Dimens2.spaceMd))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            val imgModel: Any = if (activeImage.path.startsWith("file:///android_asset/") || activeImage.isRemote) {
                activeImage.path
            } else {
                File(activeImage.path)
            }
            AsyncImage(
                model = imgModel,
                contentDescription = stringResource(R.string.exercise_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.6f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Photo Count Badge (Top-Left)
            PhotoCountBadge(
                currentIndex = activeIndex,
                totalCount = images.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens2.spaceGapIconTextMedium)
            )

            // Carousel Navigation Arrows (Left & Right)
            CarouselNavigationArrows(
                currentIndex = activeIndex,
                totalCount = images.size,
                onIndexChanged = { selectedIndex = it }
            )
        }

        // Thumbnails Strip
        if (images.size > 1) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens2.inputIconSpacing),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.inputIconSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(images) { index, img ->
                    val isSelected = index == activeIndex
                    val border = if (isSelected) {
                        BorderStroke(Dimens2.shadowSm, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    val thumbModel: Any = if (img.path.startsWith("file:///android_asset/") || img.isRemote) {
                        img.path
                    } else {
                        File(img.path)
                    }
                    Box(
                        modifier = Modifier
                            .size(thumbnailSize)
                            .clip(RoundedCornerShape(Dimens2.spaceSm))
                            .border(border, RoundedCornerShape(Dimens2.spaceSm))
                            .clickable { selectedIndex = index }
                    ) {
                        AsyncImage(
                            model = thumbModel,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            )
                        }
                    }
                }
            }
        }
    }
}
