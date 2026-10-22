package com.app.exoplanethunter.presentation.screens.starsystem

import com.app.exoplanethunter.R
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.CoolBlue
import com.app.exoplanethunter.presentation.theme.FrozenBlue
import com.app.exoplanethunter.presentation.theme.HotOrange
import com.app.exoplanethunter.presentation.theme.ScorchingRed
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.StarWhite
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.TemperateGreen
import com.app.exoplanethunter.presentation.theme.WarmYellow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ===========================================================================
// Star drawing helper — corona, glow layers, core
// ===========================================================================

internal fun DrawScope.drawStar(
    cx: Float, cy: Float,
    radius: Float,
    starColor: Color,
    glowPulse: Float,
    coronaRotation: Float,
    isPrimary: Boolean
) {
    // Corona spikes
    val spikeCount = if (isPrimary) 12 else 8
    val spikeLength = radius * (if (isPrimary) 1.8f else 1.4f) * glowPulse
    val coronaAngleRad = coronaRotation * (PI.toFloat() / 180f)

    for (i in 0 until spikeCount) {
        val spikeAngle = coronaAngleRad + i * (2f * PI.toFloat() / spikeCount)
        val endX = cx + spikeLength * cos(spikeAngle)
        val endY = cy + spikeLength * sin(spikeAngle)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    starColor.copy(alpha = 0.25f * glowPulse),
                    Color.Transparent
                ),
                start = Offset(cx, cy),
                end = Offset(endX, endY)
            ),
            start = Offset(cx, cy),
            end = Offset(endX, endY),
            strokeWidth = if (i % 2 == 0) 2.5f else 1.2f,
            cap = StrokeCap.Round
        )
    }

    // Glow layers
    val glowLayers = 5
    for (i in glowLayers downTo 1) {
        val glowRadius = radius + (radius * 0.7f * i / glowLayers) * glowPulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    starColor.copy(alpha = 0.15f / i * glowPulse),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(cx, cy)
        )
    }

    // Star body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.9f),
                starColor.copy(alpha = 0.95f),
                starColor.copy(alpha = 0.8f)
            ),
            center = Offset(cx - radius * 0.15f, cy - radius * 0.15f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

// ===========================================================================
// Planet detail drawing helpers
// ===========================================================================

// ===========================================================================
// Enhanced planet label with data
// ===========================================================================

// ===========================================================================
// Color helpers — richer and data-driven
// ===========================================================================

internal fun getStarColor(tempK: Double?): Color {
    if (tempK == null) return StarGold
    return when {
        tempK > 30000 -> Color(0xFF9BB0FF) // O type - blue
        tempK > 10000 -> CoolBlue           // B type - blue-white
        tempK > 7500 -> Color(0xFFCAD8FF)   // A type - white-blue
        tempK > 6000 -> StarWhite           // F type - yellowish-white
        tempK > 5200 -> StarGold            // G type - yellow (Sun-like)
        tempK > 3700 -> SolarOrange         // K type - orange
        tempK > 2400 -> ScorchingRed        // M type - red dwarf
        else -> Color(0xFF8B4513)           // L/T/Y type - brown dwarf
    }
}

/**
 * More nuanced planet color that considers temperature, radius, and mass.
 */
internal fun getPlanetColorDetailed(
    tempK: Double?,
    radiusEarth: Double?,
    massEarth: Double?
): Color {
    val r = radiusEarth ?: 1.0
    val m = massEarth ?: 1.0

    // Gas giants get banding-style colors
    if (r > 8.0) {
        return when {
            tempK != null && tempK > 1500 -> Color(0xFFE84040) // Ultra-hot Jupiter
            tempK != null && tempK > 1000 -> Color(0xFFFF6B40) // Hot Jupiter
            tempK != null && tempK > 500 -> Color(0xFFCC8844)  // Warm Jupiter
            else -> Color(0xFFBB9955)                          // Cool Jupiter
        }
    }

    // Ice giants
    if (r in 3.0..8.0) {
        return when {
            tempK != null && tempK > 800 -> Color(0xFF7B68EE)  // Hot Neptune
            tempK != null && tempK > 400 -> Color(0xFF5B9BD5)  // Warm Neptune
            else -> Color(0xFF4FC3F7)                          // Cold Neptune / Uranus-like
        }
    }

    // Rocky / Super-Earth
    if (tempK == null) return Color(0xFFAAAAAA)
    return when {
        tempK < 150 -> Color(0xFF88BBEE) // Frozen world — icy blue
        tempK < 220 -> FrozenBlue
        tempK < 280 -> CoolBlue
        tempK < 320 -> TemperateGreen    // Potentially habitable zone
        tempK < 400 -> Color(0xFFAACC44) // Warm temperate
        tempK < 600 -> WarmYellow
        tempK < 900 -> HotOrange
        tempK < 1500 -> ScorchingRed
        else -> Color(0xFFDD3333)         // Lava world
    }
}

