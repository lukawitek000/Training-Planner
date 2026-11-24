package com.lukasz.witkowski.training.planner.exercise.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import java.io.File

/**
 * Reusable exercise image component that loads an [ImageReference] from bundled assets,
 * local storage, content URIs, or remote URLs, falling back to the default exercise graphic.
 */
@Composable
fun AppExerciseImage(
    imageReference: ImageReference?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = Color.Unspecified,
    contentScale: ContentScale = ContentScale.Fit
) {
    val boxModifier = if (containerColor != Color.Unspecified) {
        modifier
            .clip(shape)
            .background(containerColor)
    } else {
        modifier
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = boxModifier
    ) {
        if (imageReference != null) {
            val imgModel: Any = if (
                imageReference.path.startsWith("file:///android_asset/") ||
                imageReference.isRemote ||
                imageReference.path.startsWith("content://")
            ) {
                imageReference.path
            } else {
                File(imageReference.path)
            }
            AsyncImage(
                model = imgModel,
                placeholder = painterResource(id = R.drawable.exercise_placeholder),
                error = painterResource(id = R.drawable.exercise_error),
                fallback = painterResource(id = R.drawable.exercise_fallback),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.clip(shape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.exercise_fallback),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.clip(shape)
            )
        }
    }
}

@AppPreview
@Composable
internal fun AppExerciseImagePreview() {
    TrainingPlannerTheme {
        AppExerciseImage(
            imageReference = null,
            contentDescription = "Default Exercise",
            modifier = Modifier.size(Dimens.listItemThumbnailSize)
        )
    }
}
