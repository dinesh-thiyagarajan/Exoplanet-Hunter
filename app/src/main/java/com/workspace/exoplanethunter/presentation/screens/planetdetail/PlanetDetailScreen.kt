package com.workspace.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.workspace.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.workspace.exoplanethunter.presentation.components.HabitabilityScoreBar
import com.workspace.exoplanethunter.presentation.components.Planet3DRenderer
import com.workspace.exoplanethunter.presentation.components.StarField
import com.workspace.exoplanethunter.ads.AdBannerCard
import com.workspace.exoplanethunter.presentation.theme.AuroraGreen
import com.workspace.exoplanethunter.presentation.theme.CautionYellow
import com.workspace.exoplanethunter.presentation.theme.CosmicCyan
import com.workspace.exoplanethunter.presentation.theme.HabitableGreen
import com.workspace.exoplanethunter.presentation.theme.HostileRed
import com.workspace.exoplanethunter.presentation.theme.NebulaPink
import com.workspace.exoplanethunter.presentation.theme.SolarOrange
import com.workspace.exoplanethunter.presentation.theme.SpaceBlack
import com.workspace.exoplanethunter.presentation.theme.StarGold
import com.workspace.exoplanethunter.presentation.theme.SurfaceCard
import com.workspace.exoplanethunter.presentation.theme.SurfaceCardLight
import com.workspace.exoplanethunter.presentation.theme.TextMuted
import com.workspace.exoplanethunter.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PlanetDetailScreen(
    planetId: Long,
    onBack: () -> Unit,
    viewModel: PlanetDetailViewModel = viewModel(factory = PlanetDetailViewModel.Factory)
) {
    LaunchedEffect(planetId) {
        viewModel.loadPlanet(planetId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        StarField(starCount = 80)

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CosmicCyan)
            }
        } else {
            val planet = viewModel.planet ?: return@Box
            val insight = viewModel.insight

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
                    Text(
                        text = planet.planetName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 3D Planet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Planet3DRenderer(
                        planet = planet,
                        size = 280.dp,
                        enableRotation = true,
                        autoRotate = true
                    )
                }

                Text(
                    text = "Drag to rotate",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Classification badge
                insight?.let { ins ->
                    ClassificationBadge(
                        classification = ins.classification,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )
                }

                // Content
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ML Habitability Insight
                    insight?.let { ins ->
                        AnimatedSection(delay = 0) {
                            HabitabilityCard(insight = ins)
                        }
                    }

                    // Planet Properties
                    AnimatedSection(delay = 100) {
                        PlanetPropertiesCard(planet = planet)
                    }

                    // Ad banner between property cards
                    AdBannerCard()

                    // Stellar Properties
                    AnimatedSection(delay = 200) {
                        StellarPropertiesCard(planet = planet)
                    }

                    // Discovery Info
                    AnimatedSection(delay = 300) {
                        DiscoveryCard(planet = planet)
                    }

                    // ML Insights
                    insight?.let { ins ->
                        if (ins.insights.isNotEmpty()) {
                            AnimatedSection(delay = 400) {
                                InsightsCard(insights = ins.insights)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun AnimatedSection(delay: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
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

@Composable
private fun ClassificationBadge(
    classification: PlanetClassification,
    modifier: Modifier = Modifier
) {
    val color = when (classification) {
        PlanetClassification.POTENTIALLY_HABITABLE -> HabitableGreen
        PlanetClassification.ROCKY -> CosmicCyan
        PlanetClassification.SUPER_EARTH -> AuroraGreen
        PlanetClassification.SUB_EARTH -> CautionYellow
        PlanetClassification.NEPTUNE_LIKE -> NebulaPink
        PlanetClassification.GAS_GIANT -> SolarOrange
        PlanetClassification.UNKNOWN -> TextMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = classification.label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HabitabilityCard(insight: HabitabilityInsight) {
    DetailCard(
        title = "ML Habitability Analysis",
        icon = Icons.Default.Star,
        iconColor = StarGold
    ) {
        // Overall score
        val scoreColor = when {
            insight.overallScore > 0.7 -> HabitableGreen
            insight.overallScore > 0.4 -> CautionYellow
            else -> HostileRed
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                scoreColor.copy(alpha = 0.3f),
                                scoreColor.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(insight.overallScore * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Habitability Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = when {
                        insight.overallScore > 0.7 -> "High potential for habitability"
                        insight.overallScore > 0.4 -> "Moderate habitability potential"
                        insight.overallScore > 0.2 -> "Low habitability potential"
                        else -> "Hostile to known life"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Individual scores
        insight.scores.forEach { (label, score) ->
            HabitabilityScoreBar(
                label = label,
                score = score,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanetPropertiesCard(planet: Exoplanet) {
    DetailCard(
        title = "Planet Properties",
        icon = Icons.Default.Info,
        iconColor = CosmicCyan
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            planet.planetRadiusEarth?.let {
                PropertyItem("Radius", "${String.format("%.2f", it)} R\u2295", "Earth radii")
            }
            planet.planetMassEarth?.let {
                PropertyItem("Mass", "${String.format("%.2f", it)} M\u2295", "Earth masses")
            }
            planet.orbitalPeriodDays?.let {
                PropertyItem("Orbit Period", "${String.format("%.2f", it)} days", "")
            }
            planet.orbitSemiMajorAxisAu?.let {
                PropertyItem("Semi-Major Axis", "${String.format("%.4f", it)} AU", "")
            }
            planet.eccentricity?.let {
                PropertyItem("Eccentricity", String.format("%.4f", it), "")
            }
            planet.equilibriumTempK?.let {
                PropertyItem("Eq. Temperature", "${it.toInt()} K", "${(it - 273.15).toInt()}\u00B0C")
            }
            planet.insolationFlux?.let {
                PropertyItem("Insolation", "${String.format("%.2f", it)} S\u2295", "Solar flux")
            }
            planet.planetRadiusJupiter?.let {
                PropertyItem("Radius (Jup)", "${String.format("%.3f", it)} R\u2C7F", "Jupiter radii")
            }
            planet.planetMassJupiter?.let {
                PropertyItem("Mass (Jup)", "${String.format("%.4f", it)} M\u2C7F", "Jupiter masses")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StellarPropertiesCard(planet: Exoplanet) {
    DetailCard(
        title = "Host Star: ${planet.hostName}",
        icon = Icons.Default.Star,
        iconColor = SolarOrange
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            planet.spectralType?.let {
                PropertyItem("Spectral Type", it, "")
            }
            planet.stellarEffectiveTempK?.let {
                PropertyItem("Temperature", "${it.toInt()} K", "Effective")
            }
            planet.stellarRadiusSolar?.let {
                PropertyItem("Radius", "${String.format("%.3f", it)} R\u2609", "Solar radii")
            }
            planet.stellarMassSolar?.let {
                PropertyItem("Mass", "${String.format("%.3f", it)} M\u2609", "Solar masses")
            }
            planet.stellarMetallicity?.let {
                PropertyItem("Metallicity", String.format("%.3f", it), "[Fe/H]")
            }
            planet.stellarSurfaceGravity?.let {
                PropertyItem("Surface Gravity", String.format("%.3f", it), "log(g)")
            }
            PropertyItem("Stars in System", planet.numStars.toString(), "")
            PropertyItem("Planets in System", planet.numPlanets.toString(), "")
        }
    }
}

@Composable
private fun DiscoveryCard(planet: Exoplanet) {
    DetailCard(
        title = "Discovery",
        icon = Icons.Default.LocationOn,
        iconColor = NebulaPink
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Method", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    planet.discoveryMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Year", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    planet.discoveryYear.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Facility", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            planet.discoveryFacility,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        planet.distanceParsec?.let { dist ->
            Spacer(modifier = Modifier.height(12.dp))
            Text("Distance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                "${String.format("%.2f", dist)} parsecs (${String.format("%.1f", dist * 3.26156)} light-years)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        if (planet.ra != null && planet.dec != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Coordinates", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                "RA: ${String.format("%.5f", planet.ra)}\u00B0  Dec: ${String.format("%.5f", planet.dec)}\u00B0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
    DetailCard(
        title = "ML Analysis Insights",
        icon = Icons.Default.Info,
        iconColor = AuroraGreen
    ) {
        insights.forEachIndexed { index, insight ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CosmicCyan)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

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
