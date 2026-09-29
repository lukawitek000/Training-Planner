package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OverlayContent(
    modifier: Modifier = Modifier,
    backgroundContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        backgroundContent()
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)) // Dims out the background
                .clickable(
                    enabled = false,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun OverlayLoading(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OverlayContent(
        modifier = modifier,
        backgroundContent = content,
    ) {
        CircularProgressIndicator()
    }
}
