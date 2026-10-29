package com.app.exoplanethunter.presentation.theme

import android.app.Activity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MatchaNightColorScheme = darkColorScheme(
    primary = Matcha,
    onPrimary = OnMatcha,
    primaryContainer = MatchaDeep,
    onPrimaryContainer = OnMatchaDeep,
    secondary = Matcha,
    onSecondary = OnMatcha,
    secondaryContainer = MatchaDeep,
    onSecondaryContainer = OnMatchaDeep,
    tertiary = StarOrange,
    onTertiary = OnMatcha,
    background = Ink,
    onBackground = InkText,
    surface = Ink,
    onSurface = InkText,
    surfaceVariant = Surface,
    onSurfaceVariant = InkTextDim,
    surfaceContainerLowest = Well,
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Raised,
    surfaceContainerHighest = Raised,
    inverseSurface = InkText,
    inverseOnSurface = Ink,
    error = TempScorched,
    onError = Ink,
    outline = InkTextFaint,
    outlineVariant = Line,
    scrim = androidx.compose.ui.graphics.Color(0x99000000),
)

/** Shapes: pills for controls, 24/28 dp cards, 32 dp dialogs. */
object Corners {
    val Pill = CircleShape
    val SmallCard = RoundedCornerShape(24.dp)
    val Card = RoundedCornerShape(28.dp)
    val Dialog = RoundedCornerShape(32.dp)
}

private val MatchaNightShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
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
        colorScheme = MatchaNightColorScheme,
        typography = Typography,
        shapes = MatchaNightShapes,
        content = content
    )
}
