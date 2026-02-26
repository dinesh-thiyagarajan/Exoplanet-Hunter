package com.workspace.exoplanethunter.presentation.screens.starsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.model.StarSystem
import com.workspace.exoplanethunter.presentation.components.StarField
import com.workspace.exoplanethunter.presentation.theme.CosmicCyan
import com.workspace.exoplanethunter.presentation.theme.CoolBlue
import com.workspace.exoplanethunter.presentation.theme.FrozenBlue
import com.workspace.exoplanethunter.presentation.theme.HotOrange
import com.workspace.exoplanethunter.presentation.theme.ScorchingRed
import com.workspace.exoplanethunter.presentation.theme.SolarOrange
import com.workspace.exoplanethunter.presentation.theme.SpaceBlack
import com.workspace.exoplanethunter.presentation.theme.StarGold
import com.workspace.exoplanethunter.presentation.theme.StarWhite
import com.workspace.exoplanethunter.presentation.theme.SurfaceCard
import com.workspace.exoplanethunter.presentation.theme.SurfaceCardLight
import com.workspace.exoplanethunter.presentation.theme.TemperateGreen
import com.workspace.exoplanethunter.presentation.theme.TextMuted
import com.workspace.exoplanethunter.presentation.theme.TextSecondary
import com.workspace.exoplanethunter.presentation.theme.WarmYellow
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun StarSystemDetailScreen(
    hostName: String,
    onPlanetClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StarSystemDetailViewModel = viewModel(factory = StarSystemDetailViewModel.Factory)
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
                        Text(
                            text = "${system.numPlanets} planet${if (system.numPlanets != 1) "s" else ""} in system",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Planet count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(StarGold.copy(alpha = 0.2f), SolarOrange.copy(alpha = 0.2f))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${system.numPlanets} planets",
                            style = MaterialTheme.typography.labelMedium,
                            color = StarGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Solar system visualization
                SolarSystemVisualization(
                    system = system,
                    onPlanetClick = onPlanetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = "Tap a planet to view details",
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
                    // Stellar properties card
                    AnimatedSection(delayMs = 0) {
                        StellarInfoCard(system = system)
                    }

                    // Planets list card
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

// ---------------------------------------------------------------------------
// Solar System Canvas Visualization
// ---------------------------------------------------------------------------

@Composable
private fun SolarSystemVisualization(
    system: StarSystem,
    onPlanetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val planets = system.planets
    val textMeasurer = rememberTextMeasurer()

    // Animate time for orbiting planets
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_anim")
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
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_glow"
    )

    // Determine star visual properties
    val starColor = getStarColor(system.stellarEffectiveTempK)
    val starGlowColor = starColor.copy(alpha = 0.3f)

    // Sort planets by orbit distance for proper layering
    val sortedPlanets = remember(planets) {
        planets.sortedBy { it.orbitSemiMajorAxisAu ?: Double.MAX_VALUE }
    }

    // Track selected planet for name label
    var selectedPlanetId by remember { mutableStateOf<Long?>(null) }

    // Planet positions for hit-testing (stored as planet id -> (x, y) on canvas)
    var planetPositions by remember { mutableStateOf<Map<Long, Offset>>(emptyMap()) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sortedPlanets) {
                    detectTapGestures { tapOffset ->
                        // Check if any planet was tapped (within ~30px radius)
                        val hitPlanet = planetPositions.entries.minByOrNull { (_, pos) ->
                            val dx = tapOffset.x - pos.x
                            val dy = tapOffset.y - pos.y
                            sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        }

                        if (hitPlanet != null) {
                            val dx = tapOffset.x - hitPlanet.value.x
                            val dy = tapOffset.y - hitPlanet.value.y
                            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            if (dist < 40f) {
                                if (selectedPlanetId == hitPlanet.key) {
                                    // Double tap navigates
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
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = min(centerX, centerY) * 0.88f

            // Calculate star size based on stellar radius (clamped)
            val stellarRadiusFactor = (system.stellarRadiusSolar ?: 1.0).toFloat().coerceIn(0.3f, 5f)
            val starRadius = (maxRadius * 0.08f * stellarRadiusFactor).coerceIn(14f, maxRadius * 0.18f)

            // Draw star glow layers
            val glowLayers = 4
            for (i in glowLayers downTo 1) {
                val glowRadius = starRadius + (starRadius * 0.8f * i / glowLayers) * glowPulse
                drawCircle(
                    color = starGlowColor.copy(alpha = 0.12f / i),
                    radius = glowRadius,
                    center = Offset(centerX, centerY)
                )
            }

            // Draw the star
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        starColor,
                        starColor.copy(alpha = 0.8f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = starRadius
                ),
                radius = starRadius,
                center = Offset(centerX, centerY)
            )

            // Calculate orbit radii for each planet
            // Use logarithmic scaling if orbits vary widely, otherwise linear
            val orbitDistances = sortedPlanets.map { it.orbitSemiMajorAxisAu ?: 1.0 }
            val minOrbitPx = starRadius + 28f
            val maxOrbitPx = maxRadius - 16f
            val orbitRange = maxOrbitPx - minOrbitPx

            val orbitRadii = if (sortedPlanets.size == 1) {
                listOf(minOrbitPx + orbitRange * 0.5f)
            } else {
                // Evenly distribute orbits with slight weighting toward actual distances
                val maxDist = orbitDistances.max()
                val minDist = orbitDistances.min()
                val distRange = (maxDist - minDist).coerceAtLeast(0.01)

                sortedPlanets.indices.map { i ->
                    val evenSpacing = i.toFloat() / (sortedPlanets.size - 1).coerceAtLeast(1)
                    val distWeight = ((orbitDistances[i] - minDist) / distRange).toFloat()
                    // Blend between even spacing and distance-based spacing
                    val blended = evenSpacing * 0.6f + distWeight * 0.4f
                    minOrbitPx + orbitRange * blended
                }
            }

            val newPositions = mutableMapOf<Long, Offset>()

            // Draw orbital rings and planets
            sortedPlanets.forEachIndexed { index, planet ->
                val orbitRadius = orbitRadii[index]

                // Draw orbit ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = orbitRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )

                // Calculate planet position on orbit
                // Different planets orbit at different speeds based on their period
                val periodFactor = (planet.orbitalPeriodDays ?: (100.0 + index * 200.0))
                val speedFactor = (400.0 / periodFactor).coerceIn(0.1, 4.0).toFloat()
                val angleOffset = index * (360f / sortedPlanets.size.coerceAtLeast(1))
                val angle = ((time * speedFactor + angleOffset) % 360f) * (PI.toFloat() / 180f)

                val planetX = centerX + orbitRadius * cos(angle)
                val planetY = centerY + orbitRadius * sin(angle)

                newPositions[planet.id] = Offset(planetX, planetY)

                // Calculate planet size based on radius (clamped)
                val radiusFactor = (planet.planetRadiusEarth ?: 1.0).toFloat()
                val planetDrawRadius = when {
                    radiusFactor < 1f -> 6f
                    radiusFactor < 2f -> 8f
                    radiusFactor < 4f -> 10f
                    radiusFactor < 8f -> 13f
                    radiusFactor < 15f -> 16f
                    else -> 20f
                }

                // Planet color based on temperature
                val planetColor = getPlanetColor(planet.equilibriumTempK)

                // Planet glow
                drawCircle(
                    color = planetColor.copy(alpha = 0.2f),
                    radius = planetDrawRadius + 6f,
                    center = Offset(planetX, planetY)
                )

                // Draw planet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            planetColor.copy(alpha = 0.95f),
                            planetColor,
                            planetColor.copy(alpha = 0.7f)
                        ),
                        center = Offset(planetX - planetDrawRadius * 0.2f, planetY - planetDrawRadius * 0.2f),
                        radius = planetDrawRadius
                    ),
                    radius = planetDrawRadius,
                    center = Offset(planetX, planetY)
                )

                // Draw selection ring if selected
                if (selectedPlanetId == planet.id) {
                    drawCircle(
                        color = CosmicCyan,
                        radius = planetDrawRadius + 4f,
                        center = Offset(planetX, planetY),
                        style = Stroke(width = 2f)
                    )
                }

                // Draw planet name label if selected
                if (selectedPlanetId == planet.id) {
                    drawPlanetLabel(
                        textMeasurer = textMeasurer,
                        planetName = planet.planetName,
                        centerX = planetX,
                        centerY = planetY,
                        planetRadius = planetDrawRadius
                    )
                }
            }

            planetPositions = newPositions
        }

        // Overlay "tap again to view" hint when a planet is selected
        selectedPlanetId?.let { selectedId ->
            val selectedPlanet = planets.find { it.id == selectedId }
            selectedPlanet?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .pointerInput(selectedId) {
                            detectTapGestures {
                                onPlanetClick(selectedId)
                            }
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(getPlanetColor(it.equilibriumTempK))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${it.planetName} - Tap to view details",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicCyan
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPlanetLabel(
    textMeasurer: TextMeasurer,
    planetName: String,
    centerX: Float,
    centerY: Float,
    planetRadius: Float
) {
    val style = TextStyle(
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
    val textLayoutResult = textMeasurer.measure(
        text = planetName,
        style = style
    )
    val textWidth = textLayoutResult.size.width.toFloat()
    val textHeight = textLayoutResult.size.height.toFloat()

    // Background pill behind the name
    val bgPadH = 8f
    val bgPadV = 3f
    val bgLeft = centerX - textWidth / 2f - bgPadH
    val bgTop = centerY - planetRadius - textHeight - 10f - bgPadV

    drawRoundRect(
        color = SpaceBlack.copy(alpha = 0.85f),
        topLeft = Offset(bgLeft, bgTop),
        size = androidx.compose.ui.geometry.Size(
            textWidth + bgPadH * 2,
            textHeight + bgPadV * 2
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            centerX - textWidth / 2f,
            bgTop + bgPadV
        )
    )
}

// ---------------------------------------------------------------------------
// Color helpers
// ---------------------------------------------------------------------------

private fun getStarColor(tempK: Double?): Color {
    if (tempK == null) return StarGold
    return when {
        tempK > 10000 -> CoolBlue               // O/B type - blue-white
        tempK > 7500 -> Color(0xFFCAD8FF)        // A type - white-blue
        tempK > 6000 -> StarWhite                // F type - white
        tempK > 5000 -> StarGold                 // G type - yellow (Sun-like)
        tempK > 3700 -> SolarOrange              // K type - orange
        else -> ScorchingRed                     // M type - red dwarf
    }
}

private fun getPlanetColor(tempK: Double?): Color {
    if (tempK == null) return Color(0xFFAAAAAA)
    return when {
        tempK < 200 -> FrozenBlue
        tempK < 300 -> CoolBlue
        tempK < 350 -> TemperateGreen
        tempK < 500 -> WarmYellow
        tempK < 1000 -> HotOrange
        else -> ScorchingRed
    }
}

// ---------------------------------------------------------------------------
// Info Cards
// ---------------------------------------------------------------------------

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
        title = "Star: ${system.hostName}",
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
                    // Planet color dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getPlanetColor(planet.equilibriumTempK))
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

// ---------------------------------------------------------------------------
// Shared card components
// ---------------------------------------------------------------------------

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
