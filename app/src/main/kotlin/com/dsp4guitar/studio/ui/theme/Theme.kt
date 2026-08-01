package com.dsp4guitar.studio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberpunkColorScheme = darkColorScheme(
    primary          = MatrixGreen,
    onPrimary        = Background,
    primaryContainer = MatrixGreenDark,
    onPrimaryContainer = MatrixGreen,
    secondary        = MatrixGreenDim,
    onSecondary      = Background,
    background       = Background,
    onBackground     = OnSurface,
    surface          = Surface,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error            = Error,
    onError          = Background,
    outline          = MatrixGreenDark,
)

@Composable
fun DSP4GuitarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        typography  = Typography,
        content     = content
    )
}
