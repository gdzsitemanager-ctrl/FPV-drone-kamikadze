package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalColorScheme = darkColorScheme(
    primary = TacticalOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF421C00),
    onPrimaryContainer = TacticalAmber,
    secondary = RadarGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00381B),
    onSecondaryContainer = RadarGreen,
    tertiary = HUDCyan,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextDim,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        content = content
    )
}
