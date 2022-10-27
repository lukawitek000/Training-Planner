package com.lukasz.witkowski.training.planner.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@SuppressLint("ConflictingOnColor")
private val ColorPalette = darkColors(
    primary = Orange,
    primaryVariant = OrangeLight,
    secondary = Orange,
    background = LightBlack,
    onPrimary = Black,


    /* Other default colors to override
    background = Color.White,
surface = Orange,
    onSurface = LightDark12,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,

    */
)

@Composable
fun TrainingPlannerTheme(
    content: @Composable() () -> Unit
) {

    MaterialTheme(
        colors = ColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}