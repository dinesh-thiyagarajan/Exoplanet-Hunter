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
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.RowName
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.PlanetDisc
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.TempIndicator
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.components.isBanded
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
            val bucket = TempBucket.of(planet.estimatedTempK())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Corners.SmallCard)
                    .clickable { onPlanetClick(planet.id) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlanetDisc(color = bucket.color, size = 36.dp, banded = planet.isBanded())
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(planet.planetName, style = RowName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val details = listOfNotNull(
                        planet.planetRadiusEarth?.let { "%.1f R\u2295".format(it) },
                        planet.orbitalPeriodDays?.let { formatOrbitalPeriod(it) },
                        planet.orbitSemiMajorAxisAu?.let { "%.3f AU".format(it) },
                    )
                    Text(details.joinToString(" · "), style = AlmanacMeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TempIndicator(bucket)
                Icon(AppIcons.ChevronRight, null, tint = InkTextFaint, modifier = Modifier.padding(start = 6.dp).size(18.dp))
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
    SectionCard {
        Text(text = title, style = CardTitle)
        Spacer(modifier = Modifier.height(12.dp))
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
            Text(label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = AlmanacCaption)
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
