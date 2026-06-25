package com.app.exoplanethunter.presentation.screens.starsystem
import androidx.compose.ui.tooling.preview.Preview
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.preview.PreviewData

import androidx.compose.ui.res.stringResource
import com.app.exoplanethunter.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.Surface
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

// ===========================================================================
// Info Cards (unchanged functionality, kept for completeness)
// ===========================================================================

@Composable
internal fun AnimatedSection(delayMs: Int, content: @Composable () -> Unit) {
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
internal fun StellarInfoCard(system: StarSystem) {
    DetailCard(
        title = when {
            system.numStars >= 3 -> stringResource(
                R.string.star_system_host_multi,
                system.hostName,
                stringResource(R.string.star_system_multiplicity_trinary)
            )
            system.numStars == 2 -> stringResource(
                R.string.star_system_host_multi,
                system.hostName,
                stringResource(R.string.star_system_multiplicity_binary)
            )
            else -> stringResource(R.string.star_system_host_single, system.hostName)
        },
        icon = Icons.Default.Star,
        iconColor = SolarOrange
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            system.spectralType?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_spectral_type), it, "")
            }
            system.stellarEffectiveTempK?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_temperature), "${it.toInt()} K", stringResource(R.string.planet_detail_subtitle_effective))
            }
            system.stellarRadiusSolar?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_radius), "${String.format("%.3f", it)} R\u2609", stringResource(R.string.planet_detail_subtitle_solar_radii))
            }
            system.stellarMassSolar?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_mass), "${String.format("%.3f", it)} M\u2609", stringResource(R.string.planet_detail_subtitle_solar_masses))
            }
            system.stellarMetallicity?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_metallicity), String.format("%.3f", it), "[Fe/H]")
            }
            system.stellarSurfaceGravity?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_surface_gravity), String.format("%.3f", it), "log(g)")
            }
            system.distanceParsec?.let { dist ->
                PropertyItem(stringResource(R.string.planet_detail_label_distance), "${String.format("%.2f", dist)} pc", "${String.format("%.1f", dist * 3.26156)} ly")
            }
            if (system.ra != null && system.dec != null) {
                PropertyItem(stringResource(R.string.star_system_label_ra), String.format("%.5f\u00B0", system.ra), "")
                PropertyItem(stringResource(R.string.star_system_label_dec), String.format("%.5f\u00B0", system.dec), "")
            }
            PropertyItem(stringResource(R.string.star_system_label_stars), system.numStars.toString(), stringResource(R.string.star_system_in_system))
            PropertyItem(stringResource(R.string.star_system_label_planets), system.numPlanets.toString(), stringResource(R.string.star_system_in_system))
        }
    }
}

@Composable
internal fun PlanetsInfoCard(
    planets: List<Exoplanet>,
    onPlanetClick: (Long) -> Unit
) {
    DetailCard(
        title = stringResource(R.string.star_system_planets_section, planets.size),
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
                        text = stringResource(R.string.star_system_view),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .border(0.5.dp, Hairline, RoundedCornerShape(8.dp))
            .padding(18.dp)
    ) {
        Text(text = title.uppercase(), style = AlmanacSectionLabel)
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Hairline))
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun PropertyItem(label: String, value: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = InkTextDim)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = AlmanacMeta)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = AlmanacData)
    }
}

@Preview
@Composable
private fun StellarInfoCardPreview() = PreviewSurface { StellarInfoCard(PreviewData.starSystem) }

@Preview
@Composable
private fun PlanetsInfoCardPreview() = PreviewSurface {
    PlanetsInfoCard(planets = PreviewData.planets, onPlanetClick = {})
}
