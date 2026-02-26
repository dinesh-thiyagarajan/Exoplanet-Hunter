package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.CoolBlue
import com.app.exoplanethunter.presentation.theme.FrozenBlue
import com.app.exoplanethunter.presentation.theme.HotOrange
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.ScorchingRed
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.StarWhite
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TemperateGreen
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import com.app.exoplanethunter.presentation.theme.WarmYellow
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun StarSystemDetailScreen(
    hostName: String,
    onPlanetClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StarSystemDetailViewModel = koinViewModel()
) {
    LaunchedEffect(hostName) {
        viewModel.loadSystem(hostName)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        StarField(starCount = 80)

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StarGold)
            }
        } else {
            val system = viewModel.starSystem ?: return@Box

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = system.hostName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val systemDesc = buildString {
                            append("${system.numPlanets} planet${if (system.numPlanets != 1) "s" else ""}")
                            if (system.numStars > 1) {
                                append(" \u2022 ${system.numStars}-star system")
                            }
                        }
                        Text(
                            text = systemDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // System type badge
                    val badgeText = when {
                        system.numStars >= 3 -> "Trinary"
                        system.numStars == 2 -> "Binary"
                        else -> "${system.numPlanets} planets"
                    }
                    val badgeColor = when {
                        system.numStars >= 3 -> NebulaPink
                        system.numStars == 2 -> SolarOrange
                        else -> StarGold
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Solar system visualization — fills most of the screen
                SolarSystemVisualization(
                    system = system,
                    onPlanetClick = onPlanetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(horizontal = 4.dp)
                )

                Text(
                    text = "Tap a planet to select \u2022 Tap again to view details",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                // Cards section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedSection(delayMs = 0) {
                        StellarInfoCard(system = system)
                    }
                    AnimatedSection(delayMs = 150) {
                        PlanetsInfoCard(
                            planets = system.planets,
                            onPlanetClick = onPlanetClick
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ===========================================================================
// Solar System Canvas Visualization — the game-like interactive view
// ===========================================================================

@Composable
private fun SolarSystemVisualization(
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
                            text = "View \u2192",
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

// ===========================================================================
// Star drawing helper — corona, glow layers, core
// ===========================================================================

private fun DrawScope.drawStar(
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

private fun DrawScope.drawGasGiantBands(
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

private fun DrawScope.drawPlanetRings(
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

private fun DrawScope.drawOrbitTrail(
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

private fun DrawScope.drawPlanetLabel(
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

private fun getStarColor(tempK: Double?): Color {
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

private fun getSecondaryStarColor(primaryTempK: Double?): Color {
    // Secondary star is typically cooler
    if (primaryTempK == null) return SolarOrange
    val secondaryTemp = primaryTempK * 0.75
    return getStarColor(secondaryTemp)
}

/**
 * More nuanced planet color that considers temperature, radius, and mass.
 */
private fun getPlanetColorDetailed(
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

private fun darkenColor(color: Color, factor: Float): Color {
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

// ===========================================================================
// Info Cards (unchanged functionality, kept for completeness)
// ===========================================================================

@Composable
private fun AnimatedSection(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StellarInfoCard(system: StarSystem) {
    DetailCard(
        title = when {
            system.numStars >= 3 -> "Stars: ${system.hostName} (Trinary)"
            system.numStars == 2 -> "Stars: ${system.hostName} (Binary)"
            else -> "Star: ${system.hostName}"
        },
        icon = Icons.Default.Star,
        iconColor = SolarOrange
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            system.spectralType?.let {
                PropertyItem("Spectral Type", it, "")
            }
            system.stellarEffectiveTempK?.let {
                PropertyItem("Temperature", "${it.toInt()} K", "Effective")
            }
            system.stellarRadiusSolar?.let {
                PropertyItem("Radius", "${String.format("%.3f", it)} R\u2609", "Solar radii")
            }
            system.stellarMassSolar?.let {
                PropertyItem("Mass", "${String.format("%.3f", it)} M\u2609", "Solar masses")
            }
            system.stellarMetallicity?.let {
                PropertyItem("Metallicity", String.format("%.3f", it), "[Fe/H]")
            }
            system.stellarSurfaceGravity?.let {
                PropertyItem("Surface Gravity", String.format("%.3f", it), "log(g)")
            }
            system.distanceParsec?.let { dist ->
                PropertyItem("Distance", "${String.format("%.2f", dist)} pc", "${String.format("%.1f", dist * 3.26156)} ly")
            }
            if (system.ra != null && system.dec != null) {
                PropertyItem("RA", String.format("%.5f\u00B0", system.ra), "")
                PropertyItem("Dec", String.format("%.5f\u00B0", system.dec), "")
            }
            PropertyItem("Stars", system.numStars.toString(), "in system")
            PropertyItem("Planets", system.numPlanets.toString(), "in system")
        }
    }
}

@Composable
private fun PlanetsInfoCard(
    planets: List<Exoplanet>,
    onPlanetClick: (Long) -> Unit
) {
    DetailCard(
        title = "Planets (${planets.size})",
        icon = Icons.Default.Info,
        iconColor = CosmicCyan
    ) {
        planets.forEach { planet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                onClick = { onPlanetClick(planet.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                getPlanetColorDetailed(
                                    planet.equilibriumTempK,
                                    planet.planetRadiusEarth,
                                    planet.planetMassEarth
                                )
                            )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = planet.planetName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            planet.planetRadiusEarth?.let {
                                Text(
                                    text = "${String.format("%.1f", it)} R\u2295",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            planet.orbitalPeriodDays?.let {
                                Text(
                                    text = "${String.format("%.1f", it)} days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            planet.orbitSemiMajorAxisAu?.let {
                                Text(
                                    text = "${String.format("%.3f", it)} AU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            planet.equilibriumTempK?.let {
                                Text(
                                    text = "${it.toInt()} K",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicCyan
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Shared card components
// ===========================================================================

@Composable
private fun DetailCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            content()
        }
    }
}

@Composable
private fun PropertyItem(label: String, value: String, subtitle: String) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardLight)
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = CosmicCyan,
                fontSize = 10.sp
            )
        }
    }
}
