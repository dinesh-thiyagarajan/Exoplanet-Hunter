package com.app.exoplanethunter.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpaceColorScheme = darkColorScheme(
    primary = CosmicCyan,
    onPrimary = SpaceBlack,
    primaryContainer = CosmicBlue,
    onPrimaryContainer = StarWhite,
    secondary = NebulaPink,
    onSecondary = SpaceBlack,
    secondaryContainer = NebulaPurple,
    onSecondaryContainer = StarWhite,
    tertiary = AuroraGreen,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = StarWhite,
    surface = SurfaceDark,
    onSurface = StarWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = HostileRed,
    onError = SpaceBlack,
    outline = TextMuted
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
        colorScheme = SpaceColorScheme,
        typography = Typography,
        content = content
    )
}
