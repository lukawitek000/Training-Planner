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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import java.io.File

internal fun ImageReference.toCoilModel(): Any =
    if (path.startsWith("file:///android_asset/") || isRemote || path.startsWith("content://")) {
        path
    } else {
        File(path)
    }

@Composable
fun ImageGallery(
    images: List<ImageReference>,
    modifier: Modifier = Modifier,
    selectedImage: ImageReference? = null,
    onSelectImage: ((ImageReference) -> Unit)? = null,
    onDeleteImage: ((ImageReference) -> Unit)? = null,
    onAddImageClicked: (() -> Unit)? = null,
    heroHeight: Dp = Dimens2.heroImageHeightStandard,
    thumbnailSize: Dp = Dimens2.mediaThumbnailSize,
    showGradientOverlay: Boolean = (onDeleteImage == null && onAddImageClicked == null),
    contentScale: ContentScale = ContentScale.Fit
) {
    if (images.isEmpty()) return

    var internalIndex by remember { mutableIntStateOf(0) }
    val activeImage =
        selectedImage ?: images.getOrNull(internalIndex) ?: images.firstOrNull() ?: return
    val activeIndex = images.indexOf(activeImage).coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (images.size > 1) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    val onSelect: (ImageReference) -> Unit = { newImage ->
        if (onSelectImage != null) {
            onSelectImage(newImage)
        } else {
            internalIndex = images.indexOf(newImage).coerceAtLeast(0)
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
            AsyncImage(
                model = activeImage.toCoilModel(),
                contentDescription = stringResource(R.string.exercise_image),
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )

            // Optional Gradient Overlay
            if (showGradientOverlay) {
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
            }

            // Photo Count Badge (Top-Left)
            PhotoCountBadge(
                currentIndex = activeIndex,
                totalCount = images.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens2.spaceGapIconTextMedium)
            )

            // Optional Delete Button (Top-Right)
            if (onDeleteImage != null) {
                IconButton(
                    onClick = { onDeleteImage(activeImage) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens2.spaceSm)
                        .size(Dimens2.actionIconBoxSize)
                        .background(
                            MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.75f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_current_image),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(Dimens2.iconSize18)
                    )
                }
            }

            // Carousel Navigation Arrows (Left & Right)
            CarouselNavigationArrows(
                currentIndex = activeIndex,
                totalCount = images.size,
                onIndexChanged = { newIdx ->
                    if (newIdx in images.indices) {
                        onSelect(images[newIdx])
                    }
                }
            )
        }

        // Thumbnails Strip (shown if multiple images or add button is enabled)
        if (images.size > 1 || onAddImageClicked != null) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens2.inputIconSpacing),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.inputIconSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(images) { index, img ->
                    val isSelected = img == activeImage
                    val border = if (isSelected) {
                        BorderStroke(Dimens2.shadowSm, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(
                            Dimens2.thinBorder,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(thumbnailSize)
                            .clip(RoundedCornerShape(Dimens2.spaceSm))
                            .border(border, RoundedCornerShape(Dimens2.spaceSm))
                            .clickable { onSelect(img) }
                    ) {
                        AsyncImage(
                            model = img.toCoilModel(),
                            contentDescription = null,
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                            )
                        }
                    }
                }

                // Optional "Add More" Tile
                if (onAddImageClicked != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(thumbnailSize)
                                .clip(RoundedCornerShape(Dimens2.spaceSm))
                                .border(
                                    Dimens2.thinBorder,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(Dimens2.spaceSm)
                                )
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable(onClick = onAddImageClicked),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_more_images),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Dimens2.inputIconSize)
                                )
                                Text(
                                    text = stringResource(R.string.add_image),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
