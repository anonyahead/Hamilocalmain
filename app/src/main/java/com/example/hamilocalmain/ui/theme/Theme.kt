package com.example.hamilocalmain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextPrimary,
    secondary = SecondaryOrange,
    onSecondary = TextPrimary,
    tertiary = AccentTeal,
    onTertiary = Background,
    background = Background,
    surface = Surface,
    error = Error,
    onError = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary
)

/**
 * Root theme for Hami Local. Agricultural green and orange palette. Dark only.
 */
@Composable
fun HamiLocalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
