package com.lukasz.witkowski.training.planner.image.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2

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
        ImageGallery(
            images = images,
            selectedImage = previewImage,
            onSelectImage = onSelectPreview,
            onDeleteImage = onRemoveImageClicked,
            onAddImageClicked = onAddImagesClicked,
            heroHeight = Dimens2.mediaEmptyContainerMinHeight,
            thumbnailSize = Dimens2.mediaThumbnailSize,
            showGradientOverlay = false,
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
    ImageGallery(
        images = images,
        selectedImage = previewImage,
        onSelectImage = onSelectPreview,
        heroHeight = Dimens2.heroImageHeightStandard,
        thumbnailSize = Dimens2.mediaThumbnailSize,
        showGradientOverlay = false,
        modifier = modifier
    )
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
