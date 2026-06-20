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

internal fun DrawScope.drawGasGiantBands(
    cx: Float, cy: Float,
    radius: Float,
    planetColor: Color,
    time: Float
) {
    val bandCount = 5
    for (i in 0 until bandCount) {
        val bandY = cy + radius * (-0.7f + i * 0.28f)
        val distFromCenter = abs(bandY - cy) / radius
        if (distFromCenter > 0.92f) continue

        val bandRadius = radius * sqrt(1f - distFromCenter * distFromCenter)
        val bandAlpha = 0.12f + (i % 3) * 0.04f

        val bandColor = if (i % 2 == 0) {
            darkenColor(planetColor, 0.7f).copy(alpha = bandAlpha)
        } else {
            lightenColor(planetColor, 0.3f).copy(alpha = bandAlpha * 0.6f)
        }

        drawLine(
            color = bandColor,
            start = Offset(cx - bandRadius, bandY),
            end = Offset(cx + bandRadius, bandY),
            strokeWidth = radius * 0.08f,
            cap = StrokeCap.Round
        )
    }
}

internal fun DrawScope.drawPlanetRings(
    cx: Float, cy: Float,
    radius: Float,
    planetColor: Color
) {
    for (i in 1..3) {
        val ringRadius = radius * (1.3f + i * 0.15f)
        val ringHeight = ringRadius * 0.25f
        val ringAlpha = 0.2f - i * 0.04f

        drawOval(
            color = planetColor.copy(alpha = ringAlpha.coerceAtLeast(0.04f)),
            topLeft = Offset(cx - ringRadius, cy - ringHeight),
            size = Size(ringRadius * 2, ringHeight * 2),
            style = Stroke(width = radius * 0.04f)
        )
    }
}

internal fun DrawScope.drawOrbitTrail(
    cx: Float, cy: Float,
    orbitRadius: Float,
    ovalScaleY: Float,
    currentAngle: Float,
    planetColor: Color,
    isSelected: Boolean
) {
    val trailSegments = 20
    val trailArcLength = 0.8f // radians
    val alpha = if (isSelected) 0.3f else 0.12f

    for (i in 0 until trailSegments) {
        val frac = i.toFloat() / trailSegments
        val a = currentAngle - trailArcLength * frac
        val nextA = currentAngle - trailArcLength * (frac + 1f / trailSegments)

        val x1 = cx + orbitRadius * cos(a)
        val y1 = cy + orbitRadius * ovalScaleY * sin(a)
        val x2 = cx + orbitRadius * cos(nextA)
        val y2 = cy + orbitRadius * ovalScaleY * sin(nextA)

        drawLine(
            color = planetColor.copy(alpha = alpha * (1f - frac)),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 2f * (1f - frac * 0.5f),
            cap = StrokeCap.Round
        )
    }
}

// ===========================================================================
// Enhanced planet label with data
// ===========================================================================

internal fun DrawScope.drawPlanetLabel(
    textMeasurer: TextMeasurer,
    planet: Exoplanet,
    centerX: Float,
    centerY: Float,
    planetRadius: Float
) {
    val nameStyle = TextStyle(
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    val nameLayout = textMeasurer.measure(planet.planetName, nameStyle)

    // Build a short info line
    val infoStr = buildString {
        planet.planetRadiusEarth?.let { append("${String.format("%.1f", it)}R\u2295") }
        planet.equilibriumTempK?.let {
            if (isNotEmpty()) append("  ")
            append("${it.toInt()}K")
        }
    }
    val infoStyle = TextStyle(
        color = CosmicCyan,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
    val infoLayout = if (infoStr.isNotEmpty()) textMeasurer.measure(infoStr, infoStyle) else null

    val totalWidth = maxOf(
        nameLayout.size.width.toFloat(),
        infoLayout?.size?.width?.toFloat() ?: 0f
    )
    val totalHeight = nameLayout.size.height.toFloat() +
            (infoLayout?.size?.height?.toFloat()?.plus(2f) ?: 0f)

    val bgPadH = 10f
    val bgPadV = 5f
    val bgLeft = centerX - totalWidth / 2f - bgPadH
    val bgTop = centerY - planetRadius - totalHeight - 14f - bgPadV

    // Background pill
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                SpaceBlack.copy(alpha = 0.9f),
                SurfaceCard.copy(alpha = 0.85f)
            ),
            startY = bgTop,
            endY = bgTop + totalHeight + bgPadV * 2
        ),
        topLeft = Offset(bgLeft, bgTop),
        size = Size(totalWidth + bgPadH * 2, totalHeight + bgPadV * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )

    // Border
    drawRoundRect(
        color = CosmicCyan.copy(alpha = 0.3f),
        topLeft = Offset(bgLeft, bgTop),
        size = Size(totalWidth + bgPadH * 2, totalHeight + bgPadV * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(width = 0.8f)
    )

    // Name text
    drawText(
        textLayoutResult = nameLayout,
        topLeft = Offset(
            centerX - nameLayout.size.width / 2f,
            bgTop + bgPadV
        )
    )

    // Info text
    infoLayout?.let {
        drawText(
            textLayoutResult = it,
            topLeft = Offset(
                centerX - it.size.width / 2f,
                bgTop + bgPadV + nameLayout.size.height + 2f
            )
        )
    }
}

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

internal fun getSecondaryStarColor(primaryTempK: Double?): Color {
    // Secondary star is typically cooler
    if (primaryTempK == null) return SolarOrange
    val secondaryTemp = primaryTempK * 0.75
    return getStarColor(secondaryTemp)
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

internal fun darkenColor(color: Color, factor: Float): Color {
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}

private fun lightenColor(color: Color, factor: Float): Color {
    return Color(
        red = (color.red + (1f - color.red) * factor).coerceAtMost(1f),
        green = (color.green + (1f - color.green) * factor).coerceAtMost(1f),
        blue = (color.blue + (1f - color.blue) * factor).coerceAtMost(1f),
        alpha = color.alpha
    )
}
