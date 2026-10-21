package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.image.presentation.ImageInputForm as PresentationImageInputForm
import com.lukasz.witkowski.training.planner.image.presentation.ImagesPreview as PresentationImagesPreview

@Composable
fun ImageInputForm(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
    onAddImagesClicked: () -> Unit = {},
    onRemoveImageClicked: (ImageBitmap) -> Unit = {},
    onSelectPreview: (ImageBitmap) -> Unit = {},
) {
    PresentationImageInputForm(
        images = images,
        previewImage = previewImage,
        modifier = modifier,
        onAddImagesClicked = onAddImagesClicked,
        onRemoveImageClicked = onRemoveImageClicked,
        onSelectPreview = onSelectPreview
    )
}

@Composable
fun ImagesPreview(
    images: List<ImageBitmap>,
    previewImage: ImageBitmap?,
    modifier: Modifier = Modifier,
    onSelectPreview: (ImageBitmap) -> Unit = {},
) {
    PresentationImagesPreview(
        images = images,
        previewImage = previewImage,
        modifier = modifier,
        onSelectPreview = onSelectPreview
    )
}
