package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Reusable exercise image component for workout session execution.
 * Loads offline bundled exercise illustrations from assets or displays
 * the default fallback graphic.
 */
@Composable
fun AppExerciseImage(
    imageIds: List<ImageId>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentScale: ContentScale = ContentScale.Fit
) {
    AppExerciseImage(
        imageId = imageIds.firstOrNull(),
        contentDescription = contentDescription,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentScale = contentScale
    )
}

@Composable
fun AppExerciseImage(
    imageId: ImageId?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentScale: ContentScale = ContentScale.Fit
) {
    val model = if (imageId != null) {
        "file:///android_asset/exercise_images/${imageId.value}.webp"
    } else {
        R.drawable.exercise_default
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            placeholder = painterResource(id = R.drawable.exercise_default),
            error = painterResource(id = R.drawable.exercise_default),
            fallback = painterResource(id = R.drawable.exercise_default),
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens2.spaceXs)
        )
    }
}

@AppPreview
@Composable
internal fun AppExerciseImagePreview() {
    TrainingPlannerTheme2 {
        AppExerciseImage(
            imageId = null,
            contentDescription = "Default Exercise",
            modifier = Modifier.size(Dimens2.avatarSize)
        )
    }
}
