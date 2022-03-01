package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackArrow: Boolean,
    navigateBack: () -> Unit
) {
    TopAppBar(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility (showBackArrow) {
                IconButton(onClick = { navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                }
            }
            Text(text = title)
        }
    }
}
