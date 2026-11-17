package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NevadaCineDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CinemaObsidian,
    primaryContainer = CinemaSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = CinemaGold,
    onSecondary = CinemaObsidian,
    secondaryContainer = CinemaSurface,
    onSecondaryContainer = CinemaGold,
    tertiary = LiveCrimson,
    background = CinemaObsidian,
    onBackground = TextPrimary,
    surface = CinemaMidnight,
    onSurface = TextPrimary,
    surfaceVariant = CinemaSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NevadaCineDarkColorScheme,
        typography = Typography,
        content = content
    )
}
