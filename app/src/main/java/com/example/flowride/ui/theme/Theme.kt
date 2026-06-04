package com.example.flowride.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.flowride.ui.theme.*

private val FlowRideColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = PrimaryForeground,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Color.White,
    tertiary = Accent,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextMuted,
    outline = Border,
    error = Destructive,
)

@Composable
fun FlowRideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlowRideColorScheme,
        typography = FlowRideTypography,
        shapes = FlowRideShapes,
        content = content
    )
}