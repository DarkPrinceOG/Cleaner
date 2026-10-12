package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = CoralAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3B1F24),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = SkyPhotos,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E222D),
    error = CrimsonBright,
    onError = Color.White
)

@Composable
fun StorageCleanerTheme(
    content: @Composable () -> Unit
) {
    // We enforce the requested premium dark mode with crimson accents for consistent branding
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StorageCleanerTheme(content = content)
}
