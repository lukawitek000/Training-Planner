package com.lukasz.witkowski.training.planner.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.lukasz.witkowski.training.planner.exercise.domain.ImageReference
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.io.File

@Composable
fun Image(
    imageReference: ImageReference?,
    @DrawableRes defaultImage: Int,
    contentDescriptor: String,
    modifier: Modifier = Modifier
) {
    val imgModel = imageReference?.let { File(it.absolutePath) } ?: defaultImage
    CoilImage(
        modifier = modifier,
        imageModel = imgModel,
        imageOptions = ImageOptions(contentDescription = contentDescriptor, contentScale = ContentScale.Fit),
        loading = {
            Box(modifier = Modifier.matchParentSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    )
}