package com.lukasz.witkowski.training.planner.shared.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ShapeDefault = RoundedCornerShape(4.dp)
val ShapeLg = RoundedCornerShape(8.dp)
val ShapeXl = RoundedCornerShape(12.dp)
val ShapeFull = CircleShape

val AppShapes = Shapes(
    extraSmall = ShapeDefault,
    small = ShapeLg,
    medium = ShapeXl,
    large = RoundedCornerShape(16.dp),
    extraLarge = ShapeFull,
)
