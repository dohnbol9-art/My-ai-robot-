package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RobotCyanPrimary,
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D70),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = RobotIndigoSecondary,
    onSecondary = Color(0xFF1E1E66),
    tertiary = RobotEmeraldTertiary,
    onTertiary = Color(0xFF003824),
    background = RobotBackgroundDark,
    onBackground = RobotTextPrimary,
    surface = RobotSurfaceDark,
    onSurface = RobotTextPrimary,
    surfaceVariant = RobotSurfaceVariantDark,
    onSurfaceVariant = RobotTextSecondary,
    outline = RobotCardBorder,
    error = RobotRoseError,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Keep tech dark style for AI Robot
    primary = RobotCyanPrimary,
    background = RobotBackgroundDark,
    surface = RobotSurfaceDark
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
