package com.askara.photobooth.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    secondary = Yellow400,
    onSecondary = Slate950,
    tertiary = Emerald400,
    onTertiary = Slate950,
    error = Red500,
    background = Slate50,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    outline = Slate950,
    outlineVariant = Slate300,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = White,
    secondary = Yellow400,
    onSecondary = Slate950,
    tertiary = Emerald400,
    onTertiary = Slate950,
    error = Red400,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    outline = Slate50,
    outlineVariant = Slate500,
)

@Composable
fun PhotoBoothTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrutalTypography,
        content = content
    )
}