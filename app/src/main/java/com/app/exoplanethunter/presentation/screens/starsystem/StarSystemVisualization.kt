package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.ui.res.stringResource
import com.app.exoplanethunter.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// ===========================================================================
// Solar System Canvas Visualization — the game-like interactive view
// ===========================================================================

@Composable
internal fun SolarSystemVisualization(
    system: StarSystem,
    onPlanetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val planets = system.planets
    val textMeasurer = rememberTextMeasurer()
    val numStars = system.numStars

    // -----------------------------------------------------------------------
    // Animations
    // -----------------------------------------------------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_anim")

    // Master orbit clock — slow and smooth
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_time"
    )

    // Star glow pulsation
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_glow"
    )

    // Binary star orbit angle (if numStars >= 2)
    val binaryOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "binary_orbit"
    )

    // Selection pulse for selected planet
    val selectionPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selection_pulse"
    )

    // Corona spike animation for stars
    val coronaRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "corona_rotation"
    )

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------
    val starColor = getStarColor(system.stellarEffectiveTempK)

    val sortedPlanets = remember(planets) {
        planets.sortedBy { it.orbitSemiMajorAxisAu ?: Double.MAX_VALUE }
    }

    var selectedPlanetId by remember { mutableStateOf<Long?>(null) }
    var planetPositions by remember { mutableStateOf<Map<Long, Offset>>(emptyMap()) }

    // Zoom & pan
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sortedPlanets) {
                    detectTapGestures { tapOffset ->
                        val hitPlanet = planetPositions.entries.minByOrNull { (_, pos) ->
                            val dx = tapOffset.x - pos.x
                            val dy = tapOffset.y - pos.y
                            sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        }
                        if (hitPlanet != null) {
                            val dx = tapOffset.x - hitPlanet.value.x
                            val dy = tapOffset.y - hitPlanet.value.y
                            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            if (dist < 44f) {
                                if (selectedPlanetId == hitPlanet.key) {
                                    onPlanetClick(hitPlanet.key)
                                } else {
                                    selectedPlanetId = hitPlanet.key
                                }
                            } else {
                                selectedPlanetId = null
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3f)
                        panOffset = Offset(
                            (panOffset.x + pan.x).coerceIn(-300f, 300f),
                            (panOffset.y + pan.y).coerceIn(-300f, 300f)
                        )
                    }
                }
        ) {
            val baseCenterX = size.width / 2f
            val baseCenterY = size.height / 2f
            val centerX = baseCenterX + panOffset.x
            val centerY = baseCenterY + panOffset.y
            val maxRadius = min(baseCenterX, baseCenterY) * 0.88f * zoomScale

            // ==================================================================
            // Draw stars (single, binary, or trinary)
            // ==================================================================
            val stellarRadiusFactor = (system.stellarRadiusSolar ?: 1.0).toFloat().coerceIn(0.3f, 5f)
            val baseStarRadius = (maxRadius * 0.07f * stellarRadiusFactor).coerceIn(12f, maxRadius * 0.15f)

            if (numStars >= 2) {
                // Binary / Trinary: draw orbiting stars
                val binarySeparation = baseStarRadius * 2.8f
                val binaryAngleRad = binaryOrbitAngle * (PI.toFloat() / 180f)

                // Primary star (larger)
                val primaryRadius = baseStarRadius * 1.1f
                val primaryX = centerX + binarySeparation * 0.45f * cos(binaryAngleRad)
                val primaryY = centerY + binarySeparation * 0.45f * sin(binaryAngleRad) * 0.5f // slight tilt

                // Secondary star (slightly smaller, complementary color)
                val secondaryRadius = baseStarRadius * 0.8f
                val secondaryX = centerX - binarySeparation * 0.45f * cos(binaryAngleRad)
                val secondaryY = centerY - binarySeparation * 0.45f * sin(binaryAngleRad) * 0.5f

                val secondaryStarColor = getSecondaryStarColor(system.stellarEffectiveTempK)

                // Draw binary orbit trace
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = binarySeparation * 0.45f,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 0.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 5f), 0f)
                    )
                )

                // Draw both stars with their corona and glow
                drawStar(
                    cx = primaryX, cy = primaryY,
                    radius = primaryRadius,
                    starColor = starColor,
                    glowPulse = glowPulse,
                    coronaRotation = coronaRotation,
                    isPrimary = true
                )
                drawStar(
                    cx = secondaryX, cy = secondaryY,
                    radius = secondaryRadius,
                    starColor = secondaryStarColor,
                    glowPulse = glowPulse,
                    coronaRotation = -coronaRotation * 0.7f,
                    isPrimary = false
                )

                // If trinary, add a third star orbiting farther out
                if (numStars >= 3) {
                    val tertiaryAngleRad = -binaryOrbitAngle * 0.3f * (PI.toFloat() / 180f)
                    val tertiarySep = binarySeparation * 1.6f
                    val tertiaryX = centerX + tertiarySep * cos(tertiaryAngleRad)
                    val tertiaryY = centerY + tertiarySep * sin(tertiaryAngleRad) * 0.4f
                    val tertiaryRadius = baseStarRadius * 0.55f
                    val tertiaryColor = Color(0xFFFF8A65) // dim orange-red

                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f),
                        radius = tertiarySep,
                        center = Offset(centerX, centerY),
                        style = Stroke(
                            width = 0.6f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 6f), 0f)
                        )
                    )

                    drawStar(
                        cx = tertiaryX, cy = tertiaryY,
                        radius = tertiaryRadius,
                        starColor = tertiaryColor,
                        glowPulse = glowPulse,
                        coronaRotation = coronaRotation * 0.5f,
                        isPrimary = false
                    )
                }
            } else {
                // Single star
                drawStar(
                    cx = centerX, cy = centerY,
                    radius = baseStarRadius,
                    starColor = starColor,
                    glowPulse = glowPulse,
                    coronaRotation = coronaRotation,
                    isPrimary = true
                )
            }

            // ==================================================================
            // Compute orbit radii — data-driven with log scaling
            // ==================================================================
            val orbitDistancesAu = sortedPlanets.map { it.orbitSemiMajorAxisAu ?: 1.0 }
            val innerBoundary = baseStarRadius + (if (numStars >= 2) baseStarRadius * 2.5f else 24f)
            val outerBoundary = maxRadius - 12f
            val orbitRange = outerBoundary - innerBoundary

            val orbitRadii = if (sortedPlanets.isEmpty()) {
                emptyList()
            } else if (sortedPlanets.size == 1) {
                listOf(innerBoundary + orbitRange * 0.5f)
            } else {
                // Use log-scaled distances for realistic spacing
                val logDists = orbitDistancesAu.map { ln(it.coerceAtLeast(0.001) + 1.0) }
                val minLog = logDists.min()
                val maxLog = logDists.max()
                val logRange = (maxLog - minLog).coerceAtLeast(0.001)

                sortedPlanets.indices.map { i ->
                    val logNorm = ((logDists[i] - minLog) / logRange).toFloat()
                    // Blend: 50% log-distance, 50% even spacing for readability
                    val evenSpacing = i.toFloat() / (sortedPlanets.size - 1).coerceAtLeast(1)
                    val blended = logNorm * 0.5f + evenSpacing * 0.5f
                    innerBoundary + orbitRange * blended
                }
            }

            // ==================================================================
            // Draw orbits and planets
            // ==================================================================
            val newPositions = mutableMapOf<Long, Offset>()

            sortedPlanets.forEachIndexed { index, planet ->
                val orbitRadius = orbitRadii[index]
                val smaAu = planet.orbitSemiMajorAxisAu ?: 1.0
                val ecc = (planet.eccentricity ?: 0.0).coerceIn(0.0, 0.9)

                // Draw elliptical orbit ring
                // Semi-minor = semi-major * sqrt(1 - e²)
                val ovalScaleY = sqrt(1.0 - ecc * ecc).toFloat().coerceAtLeast(0.3f)
                drawOval(
                    color = Color.White.copy(alpha = 0.07f),
                    topLeft = Offset(centerX - orbitRadius, centerY - orbitRadius * ovalScaleY),
                    size = Size(orbitRadius * 2f, orbitRadius * 2f * ovalScaleY),
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                    )
                )

                // Planet position along its elliptical orbit
                val periodDays = planet.orbitalPeriodDays ?: (100.0 + index * 200.0)
                // Kepler-like speed: inner planets orbit faster
                val speedFactor = (400.0 / periodDays).coerceIn(0.08, 5.0).toFloat()
                val angleOffset = index * (137.5f) // golden angle spread for nice initial layout
                val angle = ((time * speedFactor + angleOffset) % 360f) * (PI.toFloat() / 180f)

                val planetX = centerX + orbitRadius * cos(angle)
                val planetY = centerY + orbitRadius * ovalScaleY * sin(angle)

                newPositions[planet.id] = Offset(planetX, planetY)

                // ------------------------------------------------------------------
                // Planet size from data — continuous mapping from radius
                // ------------------------------------------------------------------
                val radiusEarth = (planet.planetRadiusEarth ?: 1.0).toFloat()
                // Map Earth radii -> pixel radius on screen (log scale for large range)
                val planetDrawRadius = when {
                    radiusEarth <= 0.5f -> 4f
                    radiusEarth <= 1.0f -> 5f + (radiusEarth - 0.5f) * 4f   // 5–7
                    radiusEarth <= 2.0f -> 7f + (radiusEarth - 1.0f) * 3f   // 7–10
                    radiusEarth <= 4.0f -> 10f + (radiusEarth - 2.0f) * 2f  // 10–14
                    radiusEarth <= 8.0f -> 14f + (radiusEarth - 4.0f) * 1f  // 14–18
                    radiusEarth <= 15.0f -> 18f + (radiusEarth - 8.0f) * 0.5f // 18–21.5
                    else -> 22f + ln(radiusEarth - 14f).coerceAtLeast(0f) * 2f // 22+
                }.coerceIn(3f, 28f) * zoomScale.coerceIn(0.7f, 1.5f)

                // ------------------------------------------------------------------
                // Planet color from real temperature data
                // ------------------------------------------------------------------
                val planetColor = getPlanetColorDetailed(
                    tempK = planet.equilibriumTempK,
                    radiusEarth = radiusEarth.toDouble(),
                    massEarth = planet.planetMassEarth
                )
                val isSelected = selectedPlanetId == planet.id

                // ------------------------------------------------------------------
                // Planet rendering: glow → shadow → body → bands/features → highlight
                // ------------------------------------------------------------------

                // Outer glow (stronger when selected)
                val glowAlpha = if (isSelected) 0.35f + selectionPulse * 0.15f else 0.15f
                val glowSize = if (isSelected) planetDrawRadius + 12f + selectionPulse * 4f
                else planetDrawRadius + 6f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            planetColor.copy(alpha = glowAlpha),
                            planetColor.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(planetX, planetY),
                        radius = glowSize
                    ),
                    radius = glowSize,
                    center = Offset(planetX, planetY)
                )

                // Planet body with 3D shading
                val lightDir = Offset(-0.35f, -0.35f) // light from top-left (from star)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            planetColor.copy(alpha = 1f),
                            planetColor.copy(alpha = 0.9f),
                            darkenColor(planetColor, 0.5f),
                            darkenColor(planetColor, 0.2f)
                        ),
                        center = Offset(
                            planetX + lightDir.x * planetDrawRadius * 0.5f,
                            planetY + lightDir.y * planetDrawRadius * 0.5f
                        ),
                        radius = planetDrawRadius * 2f
                    ),
                    radius = planetDrawRadius,
                    center = Offset(planetX, planetY)
                )

                // Gas giant bands (for large planets)
                if (radiusEarth > 4f && planetDrawRadius > 10f) {
                    drawGasGiantBands(
                        cx = planetX, cy = planetY,
                        radius = planetDrawRadius,
                        planetColor = planetColor,
                        time = time * speedFactor
                    )
                }

                // Rings for very large gas giants
                if (radiusEarth > 8f) {
                    drawPlanetRings(
                        cx = planetX, cy = planetY,
                        radius = planetDrawRadius,
                        planetColor = planetColor
                    )
                }

                // Specular highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(
                            planetX - planetDrawRadius * 0.3f,
                            planetY - planetDrawRadius * 0.3f
                        ),
                        radius = planetDrawRadius * 0.5f
                    ),
                    radius = planetDrawRadius * 0.4f,
                    center = Offset(
                        planetX - planetDrawRadius * 0.3f,
                        planetY - planetDrawRadius * 0.3f
                    )
                )

                // Selection ring with animated pulse
                if (isSelected) {
                    val ringAlpha = 0.6f + selectionPulse * 0.4f
                    drawCircle(
                        color = CosmicCyan.copy(alpha = ringAlpha),
                        radius = planetDrawRadius + 5f + selectionPulse * 2f,
                        center = Offset(planetX, planetY),
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                    // Outer ring
                    drawCircle(
                        color = CosmicCyan.copy(alpha = ringAlpha * 0.3f),
                        radius = planetDrawRadius + 10f + selectionPulse * 4f,
                        center = Offset(planetX, planetY),
                        style = Stroke(
                            width = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), selectionPulse * 8f)
                        )
                    )
                }

                // Name label for selected planet (drawn on canvas)
                if (isSelected) {
                    drawPlanetLabel(
                        textMeasurer = textMeasurer,
                        planet = planet,
                        centerX = planetX,
                        centerY = planetY,
                        planetRadius = planetDrawRadius
                    )
                }

                // Orbit trail (fading arc behind the planet)
                drawOrbitTrail(
                    cx = centerX, cy = centerY,
                    orbitRadius = orbitRadius,
                    ovalScaleY = ovalScaleY,
                    currentAngle = angle,
                    planetColor = planetColor,
                    isSelected = isSelected
                )
            }

            planetPositions = newPositions

            // AU distance labels on orbital rings
            if (zoomScale > 0.8f) {
                sortedPlanets.forEachIndexed { index, planet ->
                    val orbitRadius = orbitRadii[index]
                    val sma = planet.orbitSemiMajorAxisAu
                    if (sma != null) {
                        val labelText = "${String.format("%.2f", sma)} AU"
                        val labelStyle = TextStyle(
                            color = Color.White.copy(alpha = 0.25f),
                            fontSize = 8.sp,
                        )
                        val measured = textMeasurer.measure(labelText, labelStyle)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                centerX + orbitRadius - measured.size.width / 2f,
                                centerY - measured.size.height - 2f
                            )
                        )
                    }
                }
            }
        }

        // Bottom overlay for selected planet
        selectedPlanetId?.let { selectedId ->
            val selectedPlanet = planets.find { it.id == selectedId }
            selectedPlanet?.let { planet ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    SurfaceCard.copy(alpha = 0.95f),
                                    SurfaceCard.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .pointerInput(selectedId) {
                            detectTapGestures { onPlanetClick(selectedId) }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Planet color indicator
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    getPlanetColorDetailed(
                                        planet.equilibriumTempK,
                                        (planet.planetRadiusEarth ?: 1.0),
                                        planet.planetMassEarth
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = planet.planetName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val details = buildString {
                                planet.planetRadiusEarth?.let {
                                    append("${String.format("%.1f", it)} R\u2295")
                                }
                                planet.equilibriumTempK?.let {
                                    if (isNotEmpty()) append(" \u2022 ")
                                    append("${it.toInt()} K")
                                }
                                planet.orbitSemiMajorAxisAu?.let {
                                    if (isNotEmpty()) append(" \u2022 ")
                                    append("${String.format("%.3f", it)} AU")
                                }
                            }
                            if (details.isNotEmpty()) {
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.star_system_view_arrow),
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

