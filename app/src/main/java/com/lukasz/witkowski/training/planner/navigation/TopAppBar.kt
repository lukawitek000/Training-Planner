package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String) {
    TopAppBar(modifier = modifier) {
        Text(text = title)
    }
}
