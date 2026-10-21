package com.lukasz.witkowski.training.planner.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.presentation.Image as PresentationImage

@Composable
fun Image(
    imageReference: ImageReference?,
    @DrawableRes defaultImage: Int,
    contentDescriptor: String,
    modifier: Modifier = Modifier
) {
    PresentationImage(
        imageReference = imageReference,
        defaultImage = defaultImage,
        contentDescriptor = contentDescriptor,
        modifier = modifier
    )
}
