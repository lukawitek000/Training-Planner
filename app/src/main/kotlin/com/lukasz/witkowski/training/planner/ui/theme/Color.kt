package com.lukasz.witkowski.training.planner.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------------
// Primary
// -----------------------------------------------------------------------------

val Orange = Color(0xFFFF9800)
val OrangeLight = Color(0xFFFFC947)
val OrangeDark = Color(0xFFF57C00)

val OrangeContainer = Color(0xFF4A2A00)
val OrangeTransparent = Color(0x44FF9800)


// -----------------------------------------------------------------------------
// Dark surfaces
// -----------------------------------------------------------------------------

val Black = Color(0xFF000000)

val LightBlack = Color(0xFF121212)
val LightDark5 = Color(0xFF1E1E1E)
val LightDark12 = Color(0xFF2E2E2E)

val DarkSurface = Color(0xFF181818)
val DarkSurfaceVariant = Color(0xFF242424)
val DarkSurfaceElevated = Color(0xFF303030)


// -----------------------------------------------------------------------------
// Neutral / text
// -----------------------------------------------------------------------------

val White = Color(0xFFFFFFFF)

val LightGrey = Color(0xFFC4C4C4)
val MediumGrey = Color(0xFF9E9E9E)
val DarkGrey = Color(0xFF757575)

val DisabledGrey = Color(0xFF616161)


// -----------------------------------------------------------------------------
// Semantic colors
// -----------------------------------------------------------------------------

val Error = Color(0xFFCF6679)
val ErrorContainer = Color(0xFF4A1C24)

val Success = Color(0xFF81C784)
val SuccessContainer = Color(0xFF1B3A1E)

val Warning = Color(0xFFFFB74D)
val WarningContainer = Color(0xFF4A3000)

val Info = Color(0xFF64B5F6)
val InfoContainer = Color(0xFF163A5A)

val TrainingPlannerColorScheme = darkColorScheme(
    // Primary
    primary = Orange,
    onPrimary = Black,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OrangeLight,

    // Secondary
    secondary = OrangeLight,
    onSecondary = Black,
    secondaryContainer = LightDark12,
    onSecondaryContainer = White,

    // Tertiary
    tertiary = OrangeDark,
    onTertiary = White,
    tertiaryContainer = LightDark12,
    onTertiaryContainer = OrangeLight,

    // Background
    background = LightBlack,
    onBackground = White,

    // Surfaces
    surface = LightDark5,
    onSurface = White,

    surfaceVariant = LightDark12,
    onSurfaceVariant = LightGrey,

    // Error
    error = Error,
    onError = Black,
    errorContainer = ErrorContainer,
    onErrorContainer = White,

    // Other semantic roles
    outline = DarkGrey,
    outlineVariant = LightDark12,

    inverseSurface = White,
    inverseOnSurface = LightBlack,
    inversePrimary = OrangeDark,

    scrim = Black
)