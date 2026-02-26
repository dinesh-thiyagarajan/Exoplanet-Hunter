package com.workspace.exoplanethunter.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.presentation.theme.CoolBlue
import com.workspace.exoplanethunter.presentation.theme.FrozenBlue
import com.workspace.exoplanethunter.presentation.theme.HotOrange
import com.workspace.exoplanethunter.presentation.theme.ScorchingRed
import com.workspace.exoplanethunter.presentation.theme.TemperateGreen
import com.workspace.exoplanethunter.presentation.theme.WarmYellow
import kotlin.math.min

@Composable
fun PlanetMiniRenderer(
    planet: Exoplanet,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val planetColor = remember(planet) {
        when {
            planet.equilibriumTempK != null && planet.equilibriumTempK < 200 -> FrozenBlue
            planet.equilibriumTempK != null && planet.equilibriumTempK < 300 -> CoolBlue
            planet.equilibriumTempK != null && planet.equilibriumTempK < 400 -> TemperateGreen
            planet.equilibriumTempK != null && planet.equilibriumTempK < 600 -> WarmYellow
            planet.equilibriumTempK != null && planet.equilibriumTempK < 1000 -> HotOrange
            planet.equilibriumTempK != null -> ScorchingRed
            else -> Color(0xFF8888AA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mini_planet")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2
        val cy = this.size.height / 2
        val radius = min(cx, cy) * 0.7f

        // Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    planetColor.copy(alpha = 0.3f * glowPulse),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 1.6f
            ),
            radius = radius * 1.6f,
            center = Offset(cx, cy)
        )

        // Planet body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    planetColor.copy(alpha = 1f),
                    planetColor.copy(alpha = 0.8f),
                    Color(0xFF111122)
                ),
                center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                radius = radius * 1.8f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                radius = radius * 0.5f
            ),
            radius = radius * 0.4f,
            center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
        )
    }
}
