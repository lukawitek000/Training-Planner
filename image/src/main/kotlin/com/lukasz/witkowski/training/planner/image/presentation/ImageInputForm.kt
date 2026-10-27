package com.lukasz.witkowski.training.planner.image.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import java.io.File

private fun ImageReference.toCoilModel(): Any =
    if (path.startsWith("file:///android_asset/") || isRemote || path.startsWith("content://")) {
        path
    } else {
        File(path)
    }

@Composable
fun ImageInputForm(
    images: List<ImageReference>,
    previewImage: ImageReference?,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.add_images),
    description: String = stringResource(R.string.add_photos_cue),
    onAddImagesClicked: () -> Unit = {},
    onRemoveImageClicked: (ImageReference) -> Unit = {},
    onSelectPreview: (ImageReference) -> Unit = {},
) {
    if (images.isEmpty()) {
        EmptyImageInputCard(
            title = title,
            description = description,
            onAddImagesClicked = onAddImagesClicked,
            modifier = modifier
        )
    } else {
        PopulatedImageInputCard(
            images = images,
            previewImage = previewImage,
            onAddImagesClicked = onAddImagesClicked,
            onRemoveImageClicked = onRemoveImageClicked,
            onSelectPreview = onSelectPreview,
            modifier = modifier
        )
    }
}

@Composable
fun ImagesPreview(
    images: List<ImageReference>,
    previewImage: ImageReference?,
    modifier: Modifier = Modifier,
    onSelectPreview: (ImageReference) -> Unit = {},
) {
    if (images.isEmpty() && previewImage == null) return
    val activeImage = previewImage ?: images.firstOrNull() ?: return
    val activeIndex = images.indexOf(activeImage).coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (images.size > 1) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens2.heroImageHeightStandard)
                .clip(RoundedCornerShape(Dimens2.spaceGapIconTextMedium))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            AsyncImage(
                model = activeImage.toCoilModel(),
                contentDescription = stringResource(R.string.image_preview),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            PhotoCountBadge(
                currentIndex = activeIndex,
                totalCount = images.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens2.spaceSm)
            )
            CarouselNavigationArrows(
                currentIndex = activeIndex,
                totalCount = images.size,
                onIndexChanged = { newIdx ->
                    if (newIdx in images.indices) {
                        onSelectPreview(images[newIdx])
                    }
                }
            )
        }

        if (images.size > 1) {
            val itemSize = Dimens2.iconContainerSize
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens2.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
            ) {
                items(images) { img ->
                    val isSelected = img == activeImage
                    val border = if (isSelected) {
                        BorderStroke(Dimens2.shadowSm, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    Box(
                        modifier = Modifier
                            .size(itemSize)
                            .clip(RoundedCornerShape(Dimens2.spaceSm))
                            .border(border, RoundedCornerShape(Dimens2.spaceSm))
                            .clickable { onSelectPreview(img) }
                    ) {
                        AsyncImage(
                            model = img.toCoilModel(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
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

@Composable
private fun EmptyImageInputCard(
    title: String,
    description: String,
    onAddImagesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens2.spaceMd),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens2.mediaThumbnailSize)
                    .clip(RoundedCornerShape(Dimens2.spaceMd))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        Dimens2.thinBorder,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(Dimens2.spaceMd)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens2.spaceXl)
                )
            }
            Spacer(modifier = Modifier.height(Dimens2.spaceGapIconTextMedium))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens2.spaceXs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens2.spaceMd))
            Button(
                onClick = onAddImagesClicked,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = Dimens2.inputIconSize, vertical = Dimens2.inputIconSpacing)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens2.iconSize18)
                )
                Spacer(modifier = Modifier.width(Dimens2.spaceSm))
                Text(
                    text = stringResource(R.string.add_image),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(Dimens2.spaceGapIconTextMedium))
            Text(
                text = stringResource(R.string.can_add_multiple_img),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PopulatedImageInputCard(
    images: List<ImageReference>,
    previewImage: ImageReference?,
    onAddImagesClicked: () -> Unit,
    onRemoveImageClicked: (ImageReference) -> Unit,
    onSelectPreview: (ImageReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeImage = previewImage ?: images.firstOrNull() ?: return
    val activeIndex = images.indexOf(activeImage).coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (images.size > 1) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceGapIconTextMedium)
    ) {
        // Hero Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens2.mediaEmptyContainerMinHeight)
                .clip(RoundedCornerShape(Dimens2.spaceMd))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            AsyncImage(
                model = activeImage.toCoilModel(),
                contentDescription = stringResource(R.string.image_preview),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Top-Left Badge (1 / N)
            PhotoCountBadge(
                currentIndex = activeIndex,
                totalCount = images.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens2.spaceGapIconTextMedium)
            )

            // Top-Right Delete Button
            IconButton(
                onClick = { onRemoveImageClicked(activeImage) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens2.spaceSm)
                    .size(Dimens2.actionIconBoxSize)
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.75f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_current_image),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(Dimens2.iconSize18)
                )
            }

            // Carousel Navigation Arrows
            CarouselNavigationArrows(
                currentIndex = activeIndex,
                totalCount = images.size,
                onIndexChanged = { newIdx ->
                    if (newIdx in images.indices) {
                        onSelectPreview(images[newIdx])
                    }
                }
            )
        }

        // Horizontal Thumbnail Strip with Add Button
        val itemSize: Dp = Dimens2.mediaThumbnailSize
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.inputIconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(images) { img ->
                val isSelected = img == activeImage
                val border = if (isSelected) {
                    BorderStroke(Dimens2.shadowSm, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(Dimens2.thinBorder, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clip(RoundedCornerShape(Dimens2.spaceSm))
                        .border(border, RoundedCornerShape(Dimens2.spaceSm))
                        .clickable { onSelectPreview(img) }
                ) {
                    AsyncImage(
                        model = img.toCoilModel(),
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

            // Add More Button
            item {
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clip(RoundedCornerShape(Dimens2.spaceSm))
                        .border(
                            Dimens2.thinBorder,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(Dimens2.spaceSm)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onAddImagesClicked),
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
