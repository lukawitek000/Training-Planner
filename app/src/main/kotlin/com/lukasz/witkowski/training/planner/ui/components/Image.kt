package com.lukasz.witkowski.training.planner.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.image.ImageReference
import java.io.File

@Composable
fun Image(
    imageReference: ImageReference?,
    @DrawableRes defaultImage: Int,
    contentDescriptor: String,
    modifier: Modifier = Modifier
) {
    val imgModel: Any = when {
        imageReference == null -> defaultImage
        imageReference.path.startsWith("file:///android_asset/") || imageReference.isRemote -> imageReference.path
        else -> File(imageReference.path)
    }
    AsyncImage(
        modifier = modifier,
        model = imgModel,
        placeholder = painterResource(defaultImage),
        error = painterResource(defaultImage),
        fallback = painterResource(defaultImage),
        contentDescription = contentDescriptor,
        contentScale = ContentScale.Fit,
    )
}
