package com.app.exoplanethunter.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AlmanacColorScheme = darkColorScheme(
    primary = Brass,
    onPrimary = Ink,
    primaryContainer = SurfaceRaised,
    onPrimaryContainer = InkText,
    secondary = Brass,
    onSecondary = Ink,
    secondaryContainer = Surface,
    onSecondaryContainer = InkText,
    tertiary = TempTemperate,
    onTertiary = Ink,
    background = Ink,
    onBackground = InkText,
    surface = Surface,
    onSurface = InkText,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = InkTextDim,
    error = TempHot,
    onError = Ink,
    outline = Hairline
)

@Composable
fun ExoplanetHunterTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AlmanacColorScheme,
        typography = Typography,
        content = content
    )
}
