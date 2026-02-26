package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.CoolBlue
import com.app.exoplanethunter.presentation.theme.FrozenBlue
import com.app.exoplanethunter.presentation.theme.HotOrange
import com.app.exoplanethunter.presentation.theme.ScorchingRed
import com.app.exoplanethunter.presentation.theme.TemperateGreen
import com.app.exoplanethunter.presentation.theme.WarmYellow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Planet3DRenderer(
    planet: Exoplanet,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    enableRotation: Boolean = true,
    autoRotate: Boolean = true,
) {
    // Full 360° rotation — no clamping on Y, gentle clamp on X (tilt)
    var rotationX by remember { mutableFloatStateOf(0.3f) }
    var rotationY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "planet_rotation")
    val autoRotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(), // Full radian circle
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = getRotationDuration(planet.orbitalPeriodDays),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "auto_rotation"
    )

    val atmosphereGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "atmosphere_glow"
    )

    val planetColors = remember(planet) { getPlanetColors(planet) }
    val planetType = remember(planet) { getPlanetType(planet) }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .then(
                    if (enableRotation) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Full 360 on Y-axis (horizontal drag), wider range on X-axis
                                rotationY += dragAmount.x * 0.01f
                                rotationX += dragAmount.y * 0.008f
                                rotationX = rotationX.coerceIn(-PI.toFloat() / 2f, PI.toFloat() / 2f) // ±90°
                            }
                        }
                    } else Modifier
                )
        ) {
            val centerX = this.size.width / 2
            val centerY = this.size.height / 2
            val radius = min(centerX, centerY) * 0.75f

            // Combine manual + auto rotation (both in radians now)
            val currentRotY = if (autoRotate) {
                rotationY + autoRotateAngle
            } else {
                rotationY
            }

            // Outer glow / atmosphere
            drawAtmosphere(centerX, centerY, radius, planetColors, atmosphereGlow)

            // Main planet sphere with rotation-aware lighting
            drawPlanetSphere(centerX, centerY, radius, planetColors, currentRotY, rotationX)

            // Surface features that properly wrap around the sphere
            drawSurfaceFeatures(centerX, centerY, radius, currentRotY, rotationX, planetType, planetColors)

            // Specular highlight
            drawSpecularHighlight(centerX, centerY, radius)

            // Terminator line (day/night boundary) — subtle shadow
            drawTerminator(centerX, centerY, radius, currentRotY)

            // Ring system for gas giants
            if (planetType == PlanetType.GAS_GIANT || planetType == PlanetType.ICE_GIANT) {
                drawRings(centerX, centerY, radius, rotationX, planetColors)
            }
        }
    }
}

private fun getRotationDuration(orbitalPeriod: Double?): Int {
    return when {
        orbitalPeriod == null -> 20000
        orbitalPeriod < 1 -> 5000
        orbitalPeriod < 10 -> 10000
        orbitalPeriod < 100 -> 15000
        orbitalPeriod < 1000 -> 20000
        else -> 30000
    }
}

private fun getPlanetColors(planet: Exoplanet): PlanetColorScheme {
    val temp = planet.equilibriumTempK
    val radius = planet.planetRadiusEarth

    val baseColor = when {
        temp != null && temp < 200 -> FrozenBlue
        temp != null && temp < 300 -> CoolBlue
        temp != null && temp < 400 -> TemperateGreen
        temp != null && temp < 600 -> WarmYellow
        temp != null && temp < 1000 -> HotOrange
        temp != null -> ScorchingRed
        else -> Color(0xFF8888AA)
    }

    val secondaryColor = when {
        radius != null && radius > 6 -> Color(0xFFBB8844) // Gas giant banding
        radius != null && radius > 3 -> Color(0xFF6688BB) // Ice giant
        temp != null && temp in 250.0..350.0 -> Color(0xFF2266AA) // Oceans
        temp != null && temp < 200.0 -> Color(0xFFCCDDFF) // Ice
        else -> baseColor.copy(alpha = 0.7f)
    }

    val atmosphereColor = when {
        temp != null && temp > 800 -> Color(0x44FF4400)
        temp != null && temp in 250.0..350.0 -> Color(0x4444AAFF)
        radius != null && radius > 6 -> Color(0x44FFAA44)
        else -> Color(0x224488CC)
    }

    return PlanetColorScheme(baseColor, secondaryColor, atmosphereColor)
}

private fun getPlanetType(planet: Exoplanet): PlanetType {
    val radius = planet.planetRadiusEarth
    val mass = planet.planetMassEarth

    return when {
        radius != null && radius > 8 -> PlanetType.GAS_GIANT
        radius != null && radius > 4 -> PlanetType.ICE_GIANT
        radius != null && radius > 1.8 -> PlanetType.SUPER_EARTH
        radius != null && radius > 0.5 -> PlanetType.ROCKY
        mass != null && mass > 300 -> PlanetType.GAS_GIANT
        mass != null && mass > 15 -> PlanetType.ICE_GIANT
        else -> PlanetType.ROCKY
    }
}

private fun DrawScope.drawAtmosphere(
    cx: Float, cy: Float, radius: Float,
    colors: PlanetColorScheme, glowIntensity: Float
) {
    val glowRadius = radius * 1.3f * glowIntensity
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.atmosphere.copy(alpha = 0.4f * glowIntensity),
                colors.atmosphere.copy(alpha = 0.15f * glowIntensity),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawPlanetSphere(
    cx: Float, cy: Float, radius: Float,
    colors: PlanetColorScheme,
    rotY: Float, rotX: Float
) {
    // Light position shifts as planet rotates, giving a true 360° feel
    val lightAngle = -0.6f // fixed light direction (from "star")
    val lightOffsetX = -radius * 0.3f * cos(rotY * 0.15f)
    val lightOffsetY = -radius * 0.3f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.primary.copy(alpha = 1f),
                colors.primary.copy(alpha = 0.95f),
                colors.secondary.copy(alpha = 0.9f),
                colors.primary.copy(alpha = 0.7f),
                Color(0xFF111122)
            ),
            center = Offset(cx + lightOffsetX, cy + lightOffsetY),
            radius = radius * 1.8f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawSurfaceFeatures(
    cx: Float, cy: Float, radius: Float,
    rotY: Float, rotX: Float,
    planetType: PlanetType,
    colors: PlanetColorScheme
) {
    when (planetType) {
        PlanetType.GAS_GIANT -> drawGasGiantBands(cx, cy, radius, rotX, rotY, colors)
        PlanetType.ICE_GIANT -> drawIceGiantBands(cx, cy, radius, rotX, rotY, colors)
        PlanetType.ROCKY, PlanetType.SUPER_EARTH -> drawRockyFeatures(cx, cy, radius, rotY, rotX, colors)
    }
}

private fun DrawScope.drawGasGiantBands(
    cx: Float, cy: Float, radius: Float,
    rotX: Float, rotY: Float,
    colors: PlanetColorScheme
) {
    val bandCount = 8
    for (i in 0 until bandCount) {
        val baseBandPos = -0.8f + i * 0.2f
        // Tilt bands with vertical rotation
        val bandY = cy + radius * (baseBandPos + sin(rotX) * 0.15f)
        val bandWidth = radius * 0.06f
        val distFromCenter = abs(bandY - cy) / radius
        if (distFromCenter > 0.95f) continue

        val bandRadius = radius * sqrt(1f - distFromCenter * distFromCenter)
        val bandAlpha = 0.15f + (i % 3) * 0.05f

        val bandColor = if (i % 2 == 0) {
            colors.secondary.copy(alpha = bandAlpha)
        } else {
            colors.primary.copy(alpha = bandAlpha * 0.7f)
        }

        drawLine(
            color = bandColor,
            start = Offset(cx - bandRadius, bandY),
            end = Offset(cx + bandRadius, bandY),
            strokeWidth = bandWidth
        )
    }

    // Great Red Spot — wraps around with rotation
    val spotAngle = rotY
    val spotX = cx + radius * 0.4f * cos(spotAngle)
    val spotY = cy + radius * 0.15f
    val spotDist = sqrt((spotX - cx) * (spotX - cx) + (spotY - cy) * (spotY - cy))
    // Only visible when on the front face
    if (spotDist < radius * 0.85f && cos(spotAngle) > -0.2f) {
        val spotScale = sqrt(1f - (spotDist / radius).let { it * it })
        val spotAlpha = (0.5f * (cos(spotAngle) + 0.2f) / 1.2f).coerceIn(0f, 0.5f)
        drawOval(
            color = colors.secondary.copy(alpha = spotAlpha),
            topLeft = Offset(spotX - radius * 0.12f * spotScale, spotY - radius * 0.06f),
            size = androidx.compose.ui.geometry.Size(
                radius * 0.24f * spotScale,
                radius * 0.12f
            )
        )
    }
}

private fun DrawScope.drawIceGiantBands(
    cx: Float, cy: Float, radius: Float,
    rotX: Float, rotY: Float,
    colors: PlanetColorScheme
) {
    for (i in 0..5) {
        val bandY = cy + radius * (-0.6f + i * 0.24f + sin(rotX) * 0.08f)
        val distFromCenter = abs(bandY - cy) / radius
        if (distFromCenter > 0.95f) continue

        val bandRadius = radius * sqrt(1f - distFromCenter * distFromCenter)
        drawLine(
            color = Color(0xFF88BBDD).copy(alpha = 0.12f),
            start = Offset(cx - bandRadius, bandY),
            end = Offset(cx + bandRadius, bandY),
            strokeWidth = radius * 0.04f
        )
    }
}

private fun DrawScope.drawRockyFeatures(
    cx: Float, cy: Float, radius: Float,
    rotY: Float, rotX: Float,
    colors: PlanetColorScheme
) {
    // Features defined in 3D spherical coordinates, projected with rotation
    // Each: (longitude, latitude, size)
    val featureSeeds = listOf(
        Triple(0.0f, -0.2f, 0.25f),
        Triple(1.2f, 0.3f, 0.2f),
        Triple(2.5f, 0.1f, 0.15f),
        Triple(3.8f, -0.3f, 0.18f),
        Triple(5.0f, 0.15f, 0.12f),
        Triple(0.8f, -0.4f, 0.14f),
        Triple(2.0f, 0.4f, 0.16f),
        Triple(4.2f, -0.1f, 0.20f),
    )

    for ((longitude, latitude, featureRadius) in featureSeeds) {
        // Rotate longitude by current Y rotation
        val adjustedLon = longitude + rotY

        // Project 3D sphere position to 2D
        val cosLon = cos(adjustedLon)
        val sinLon = sin(adjustedLon)
        val cosLat = cos(latitude + rotX * 0.5f)
        val sinLat = sin(latitude + rotX * 0.5f)

        // Only draw if on front hemisphere
        if (cosLon < -0.1f) continue

        // Screen-space position
        val fx = cx + radius * sinLon * cosLat * 0.85f
        val fy = cy + radius * sinLat * 0.85f

        val distFromCenter = sqrt((fx - cx) * (fx - cx) + (fy - cy) * (fy - cy))
        if (distFromCenter > radius * 0.9f) continue

        // Foreshortening: features near the edge appear smaller
        val depthScale = cosLon.coerceAtLeast(0f)
        val scale = sqrt(1f - (distFromCenter / radius).let { it * it }) * depthScale
        val fr = radius * featureRadius * scale

        if (fr < 1f) continue

        // Fade alpha based on how much it faces the viewer
        val alpha = (0.35f * depthScale).coerceIn(0.05f, 0.35f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.secondary.copy(alpha = alpha),
                    colors.secondary.copy(alpha = alpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(fx, fy),
                radius = fr
            ),
            radius = fr,
            center = Offset(fx, fy)
        )
    }
}

private fun DrawScope.drawSpecularHighlight(cx: Float, cy: Float, radius: Float) {
    val highlightX = cx - radius * 0.35f
    val highlightY = cy - radius * 0.35f
    val highlightRadius = radius * 0.35f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(highlightX, highlightY),
            radius = highlightRadius
        ),
        radius = highlightRadius,
        center = Offset(highlightX, highlightY)
    )
}

/**
 * Subtle day/night terminator line that shifts with rotation, giving
 * the illusion of a 360° lit surface.
 */
private fun DrawScope.drawTerminator(
    cx: Float, cy: Float, radius: Float,
    rotY: Float
) {
    // The terminator is a vertical band on the dark side
    // As the planet rotates, the shadow shifts
    val shadowOffsetX = radius * 0.6f * sin(rotY * 0.3f)
    val shadowSide = cx + shadowOffsetX + radius * 0.4f

    // Dark crescent on trailing edge
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color(0x22000011),
                Color(0x55000022)
            ),
            center = Offset(shadowSide, cy),
            radius = radius * 1.4f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawRings(
    cx: Float, cy: Float, radius: Float,
    rotX: Float,
    colors: PlanetColorScheme
) {
    val ringTilt = sin(rotX) * 0.4f
    val ringCount = 3

    for (i in 1..ringCount) {
        val ringRadius = radius * (1.3f + i * 0.15f)
        val ringHeight = ringRadius * abs(ringTilt).coerceAtLeast(0.08f) * 0.3f
        val ringAlpha = 0.25f - i * 0.05f

        drawOval(
            color = colors.secondary.copy(alpha = ringAlpha.coerceAtLeast(0.05f)),
            topLeft = Offset(cx - ringRadius, cy - ringHeight),
            size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringHeight * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = radius * 0.03f
            )
        )
    }
}

private enum class PlanetType {
    ROCKY, SUPER_EARTH, ICE_GIANT, GAS_GIANT
}

private data class PlanetColorScheme(
    val primary: Color,
    val secondary: Color,
    val atmosphere: Color
)
