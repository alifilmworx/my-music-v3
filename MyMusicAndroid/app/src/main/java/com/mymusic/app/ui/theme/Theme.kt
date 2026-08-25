package com.mymusic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Orange,
    onPrimary = SurfaceWhite,
    background = SurfaceWhite,
    surface = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = Border
)

@Composable
fun MyMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
