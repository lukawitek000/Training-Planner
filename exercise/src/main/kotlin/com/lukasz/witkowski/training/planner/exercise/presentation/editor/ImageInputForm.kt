package com.lukasz.witkowski.training.planner.exercise.presentation.editor

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton

@Composable
fun ImageInputForm(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        ImagePlaceholder(previewImage = previewImage)
        if (images.isNotEmpty()) {
            ImageSelectionPanel(
                images = images,
                previewImage = previewImage,
                addImageItem = { addImageCardItem(it) }
            )
        }
    }
}

@Composable
fun ImagesPreview(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty() && previewImage == null) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        ImagePlaceholder(previewImage = previewImage)
        if (images.isNotEmpty()) {
            ImageSelectionPanel(
                images = images,
                previewImage = previewImage
            )
        }
    }
}

@Composable
private fun ImagePlaceholder(previewImage: ImageBitmap?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        if (previewImage == null) {
            ImageInputPlaceholder()
        } else {
            AsyncImage(
                model = previewImage.bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens2.heroImageHeightCompact)
            )
        }
    }
}

@Composable
private fun ImageInputPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens2.mediaEmptyContainerMinHeight)
            .padding(Dimens2.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm, Alignment.CenterVertically)
    ) {
        Surface(
            modifier = Modifier.size(Dimens2.iconContainerSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens2.iconSize24)
                )
            }
        }
        AppSecondaryButton(
            text = stringResource(R.string.add_exercise_image),
            onClick = {}
        )
        Text(
            text = stringResource(R.string.can_add_multiple_img),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImageSelectionPanel(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
    addImageItem: (LazyListScope.(Dp) -> Unit)? = null
) {
    val itemSize = Dimens2.mediaThumbnailSize
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        items(images) { img ->
            val border = if (previewImage == img) {
                BorderStroke(
                    width = Dimens2.thinBorder * 2,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                BorderStroke(
                    width = Dimens2.thinBorder,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }
            Surface(
                modifier = Modifier.size(itemSize),
                shape = MaterialTheme.shapes.small,
                border = border
            ) {
                AsyncImage(
                    model = img.bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        addImageItem?.invoke(this, itemSize)
    }
}

private fun LazyListScope.addImageCardItem(itemSize: Dp) {
    item {
        Surface(
            modifier = Modifier.size(itemSize),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens2.spaceSm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens2.inputIconSize)
                )
                Text(
                    text = stringResource(R.string.add),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun ImageInputFormPreview() {
    TrainingPlannerTheme2 {
        val bitmaps = remember { createPreviewBitmaps() }
        ImageInputForm(
            modifier = Modifier.fillMaxWidth(),
            images = bitmaps,
            previewImage = bitmaps.first()
        )
    }
}

@Preview
@Composable
private fun EmptyImageInputFormPreview() {
    TrainingPlannerTheme2 {
        ImageInputForm(
            modifier = Modifier.fillMaxWidth(),
            images = emptyList(),
            previewImage = null
        )
    }
}

fun createPreviewBitmaps(): List<ImageBitmap> {
    fun createColoredBitmap(color: Int): Bitmap {
        val bitmap = createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }
    return listOf(
        android.graphics.Color.RED,
        android.graphics.Color.BLUE,
        android.graphics.Color.GREEN
    ).map {
        ImageBitmap(createColoredBitmap(it))
    }
}
