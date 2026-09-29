package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme


@Composable
fun ImageInputForm(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ImagePlaceholder(previewImage = previewImage)
        if (images.isEmpty()) return
        ImageSelectionPanel(
            images = images,
            previewImage = previewImage,
            addImageItem = { addImageCardItem(it) },
            modifier = Modifier.padding(top = Dimens.normal)
        )
    }
}

@Composable
fun ImagesPreview(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty() && previewImage == null) return
    Column(modifier = modifier) {
        ImagePlaceholder(previewImage = previewImage)
        if (images.isEmpty()) return
        ImageSelectionPanel(
            images = images,
            previewImage = previewImage,
            modifier = Modifier.padding(top = Dimens.normal)
        )
    }
}

@Composable
private fun ImagePlaceholder(
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        if (previewImage == null) {
            ImageInputPlaceholder()
        } else {
            AsyncImage(
                model = previewImage.bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ImageInputPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.normal)
            .border(
                width = Dimens.border,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(Dimens.large)
            )
            .padding(Dimens.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            modifier = Modifier
                .size(Dimens.imagePlaceholderSize)
                .padding(bottom = Dimens.normal),
        )
        Button(
            onClick = {},
            modifier = Modifier.padding(bottom = Dimens.normal)
        ) {
            Text(stringResource(R.string.add_exercise_image))
        }
        Text(stringResource(R.string.can_add_multiple_img))
    }
}

@Composable
private fun ImageSelectionPanel(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
    addImageItem: (LazyListScope.(Dp) -> Unit)? = null,
) {
    val itemSize = 64.dp
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        items(images) { img ->
            val border = if (previewImage == img) {
                BorderStroke(width = Dimens.border, color = MaterialTheme.colorScheme.primary)
            } else {
                null
            }
            Card(
                modifier = Modifier.size(itemSize),
                border = border
            ) {
                AsyncImage(
                    model = img.bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
        addImageItem?.invoke(this, itemSize)
    }
}

private fun LazyListScope.addImageCardItem(itemSize: Dp) {
    item {
        Card(
            modifier = Modifier.size(itemSize),
            border = BorderStroke(
                width = Dimens.border,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.normal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Preview
@Composable
private fun ImageInputFormPreview() {
    TrainingPlannerTheme {
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
    TrainingPlannerTheme {
        ImageInputForm(
            modifier = Modifier.fillMaxWidth(),
            images = emptyList(),
            previewImage = null
        )
    }
}

fun createPreviewBitmaps(): List<ImageBitmap> {
    fun createBitmap(color: Int): Bitmap {
        val bitmap = createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        // Fills bitmap with a visible preview color (e.g., Red/Blue)
        bitmap.eraseColor(color)
        return bitmap
    }
    return listOf(android.graphics.Color.RED, android.graphics.Color.BLUE, android.graphics.Color.GREEN).map {
            ImageBitmap(createBitmap(it))
    }
}


