package com.lukasz.witkowski.training.planner.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.lukasz.witkowski.training.planner.image.ImageReference
import java.io.File

@Composable
fun Image(
    imageReference: ImageReference?,
    @DrawableRes defaultImage: Int,
    contentDescriptor: String,
    modifier: Modifier = Modifier
) {
    val imgModel = imageReference?.let { File(it.path) } ?: defaultImage
    AsyncImage(
        modifier = modifier,
        model = imgModel,
        contentDescription = contentDescriptor,
        contentScale = ContentScale.Fit,
//        loading = {
//            Box(modifier = Modifier.matchParentSize()) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//        }
    )
}
