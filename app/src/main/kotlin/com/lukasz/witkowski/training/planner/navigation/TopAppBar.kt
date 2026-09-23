package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackArrow: Boolean,
    navigateBack: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            AnimatedVisibility (showBackArrow) {
                IconButton(onClick = { navigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
            }
        },
        title = {
            Text(text = title)
        }
    )
}
