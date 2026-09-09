package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RpgGoldPrimary,
    onPrimary = Color.Black,
    secondary = RpgArcaneCyan,
    onSecondary = Color.Black,
    tertiary = RpgCrimson,
    onTertiary = Color.White,
    background = RpgDarkBackground,
    onBackground = RpgTextPrimary,
    surface = RpgDarkSurface,
    onSurface = RpgTextPrimary,
    surfaceVariant = RpgDarkSurfaceVariant,
    onSurfaceVariant = RpgTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
