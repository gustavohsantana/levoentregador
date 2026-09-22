package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LevoDarkColorScheme = darkColorScheme(
    primary = LevoPrimary,
    onPrimary = Color.White,
    primaryContainer = LevoPrimaryContainer,
    onPrimaryContainer = LevoOnPrimaryContainer,
    secondary = LevoPrimaryLight,
    onSecondary = Color.Black,
    tertiary = LevoInfo,
    background = LevoBackground,
    onBackground = LevoTextPrimary,
    surface = LevoSurface,
    onSurface = LevoTextPrimary,
    surfaceVariant = LevoSurfaceHigh,
    onSurfaceVariant = LevoTextSecondary,
    surfaceContainer = LevoSurfaceHigh,
    surfaceContainerHigh = LevoSurfaceHighlight,
    outline = LevoBorderSubtle,
    outlineVariant = LevoBorderFocus,
    error = LevoError,
    onError = Color.White
)

private val LevoLightColorScheme = lightColorScheme(
    primary = LevoPrimaryDark,
    onPrimary = Color.White,
    secondary = LevoPrimary,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = LevoError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LevoDarkColorScheme else LevoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
