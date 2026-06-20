package com.app.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.presentation.components.HabitabilityScoreBar
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CautionYellow
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.HabitableGreen
import com.app.exoplanethunter.presentation.theme.HostileRed
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.log10

@Composable
internal fun HabitabilityCard(insight: HabitabilityInsight) {
    var showDisclaimer by remember { mutableStateOf(false) }

    if (showDisclaimer) {
        HabitabilityDisclaimerDialog(onDismiss = { showDisclaimer = false })
    }

    DetailCard(
        title = stringResource(R.string.planet_detail_title_habitability),
        icon = Icons.Default.Star,
        iconColor = StarGold,
        headerAction = {
            IconButton(
                onClick = { showDisclaimer = true },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.planet_detail_disclaimer_action),
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    ) {
        // Overall score
        val reliable = insight.habitabilityReliable
        val scoreColor = when {
            !reliable -> TextMuted
            insight.overallScore > 0.7 -> HabitableGreen
            insight.overallScore > 0.4 -> CautionYellow
            else -> HostileRed
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                scoreColor.copy(alpha = 0.3f),
                                scoreColor.copy(alpha = 0.05f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (reliable) "${(insight.overallScore * 100).toInt()}%"
                    else stringResource(R.string.planet_detail_score_na),
                    style = if (reliable) MaterialTheme.typography.headlineMedium
                    else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.planet_detail_habitability_score),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = when {
                        !reliable -> stringResource(R.string.planet_detail_hab_desc_insufficient)
                        insight.overallScore > 0.7 -> stringResource(R.string.planet_detail_hab_desc_high)
                        insight.overallScore > 0.4 -> stringResource(R.string.planet_detail_hab_desc_moderate)
                        insight.overallScore > 0.2 -> stringResource(R.string.planet_detail_hab_desc_low)
                        else -> stringResource(R.string.planet_detail_hab_desc_hostile)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Individual scores
        insight.scores.forEach { (label, score) ->
            HabitabilityScoreBar(
                label = label,
                score = score,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun HabitabilityDisclaimerDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CosmicCyan,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.planet_detail_disclaimer_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.planet_detail_disclaimer_body),
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.planet_detail_disclaimer_dismiss),
                    color = CosmicCyan,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanetPropertiesCard(planet: Exoplanet) {
    DetailCard(
        title = stringResource(R.string.planet_detail_title_properties),
        icon = Icons.Default.Info,
        iconColor = CosmicCyan,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            planet.planetRadiusEarth?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_radius),
                    "${String.format("%.2f", it)} R\u2295",
                    stringResource(R.string.planet_detail_subtitle_earth_radii),
                )
            }
            planet.planetMassEarth?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_mass),
                    "${String.format("%.2f", it)} M\u2295",
                    stringResource(R.string.planet_detail_subtitle_earth_masses),
                )
            }
            planet.orbitalPeriodDays?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_orbit_period),
                    "${String.format("%.2f", it)} days",
                    "",
                )
            }
            planet.orbitSemiMajorAxisAu?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_semi_major_axis),
                    "${String.format("%.4f", it)} AU",
                    "",
                )
            }
            planet.eccentricity?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_eccentricity),
                    String.format("%.4f", it),
                    "",
                )
            }
            planet.equilibriumTempK?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_eq_temperature),
                    "${it.toInt()} K",
                    "${(it - 273.15).toInt()}\u00B0C",
                )
            }
            planet.insolationFlux?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_insolation),
                    "${String.format("%.2f", it)} S\u2295",
                    stringResource(R.string.planet_detail_subtitle_solar_flux),
                )
            }
            planet.planetRadiusJupiter?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_radius_jup),
                    "${String.format("%.3f", it)} R\u2C7F",
                    stringResource(R.string.planet_detail_subtitle_jupiter_radii),
                )
            }
            planet.planetMassJupiter?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_mass_jup),
                    "${String.format("%.4f", it)} M\u2C7F",
                    stringResource(R.string.planet_detail_subtitle_jupiter_masses),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StellarPropertiesCard(planet: Exoplanet) {
    DetailCard(
        title = stringResource(R.string.planet_detail_title_host_star, planet.hostName),
        icon = Icons.Default.Star,
        iconColor = SolarOrange,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            planet.spectralType?.let {
                PropertyItem(stringResource(R.string.planet_detail_label_spectral_type), it, "")
            }
            planet.stellarEffectiveTempK?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_temperature),
                    "${it.toInt()} K",
                    stringResource(R.string.planet_detail_subtitle_effective),
                )
            }
            planet.stellarRadiusSolar?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_radius),
                    "${String.format("%.3f", it)} R\u2609",
                    stringResource(R.string.planet_detail_subtitle_solar_radii),
                )
            }
            planet.stellarMassSolar?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_mass),
                    "${String.format("%.3f", it)} M\u2609",
                    stringResource(R.string.planet_detail_subtitle_solar_masses),
                )
            }
            planet.stellarMetallicity?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_metallicity),
                    String.format("%.3f", it),
                    "[Fe/H]",
                )
            }
            planet.stellarSurfaceGravity?.let {
                PropertyItem(
                    stringResource(R.string.planet_detail_label_surface_gravity),
                    String.format("%.3f", it),
                    "log(g)",
                )
            }
            PropertyItem(
                stringResource(R.string.planet_detail_label_stars_in_system),
                planet.numStars.toString(),
                "",
            )
            PropertyItem(
                stringResource(R.string.planet_detail_label_planets_in_system),
                planet.numPlanets.toString(),
                "",
            )
        }
    }
}

@Composable
internal fun DiscoveryCard(planet: Exoplanet) {
    DetailCard(
        title = stringResource(R.string.planet_detail_title_discovery),
        icon = Icons.Default.LocationOn,
        iconColor = NebulaPink,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    stringResource(R.string.planet_detail_label_method),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Text(
                    planet.discoveryMethod,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.planet_detail_label_year),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Text(
                    planet.discoveryYear.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            stringResource(R.string.planet_detail_label_facility),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Text(
            planet.discoveryFacility,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )

        planet.distanceParsec?.let { dist ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.planet_detail_label_distance),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                "${String.format("%.2f", dist)} parsecs (${
                    String.format(
                        "%.1f",
                        dist * 3.26156,
                    )
                } light-years)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }

        if (planet.ra != null && planet.dec != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.planet_detail_label_coordinates),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                "RA: ${String.format("%.5f", planet.ra)}\u00B0  Dec: ${
                    String.format(
                        "%.5f",
                        planet.dec,
                    )
                }\u00B0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
internal fun InsightsCard(insights: List<String>) {
    var showDisclaimer by remember { mutableStateOf(false) }

    if (showDisclaimer) {
        HabitabilityDisclaimerDialog(onDismiss = { showDisclaimer = false })
    }

    DetailCard(
        title = stringResource(R.string.planet_detail_title_insights),
        icon = Icons.Default.Info,
        iconColor = AuroraGreen,
        headerAction = {
            IconButton(
                onClick = { showDisclaimer = true },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.planet_detail_disclaimer_action),
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    ) {
        insights.forEachIndexed { index, insight ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CosmicCyan),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp,
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
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                headerAction?.invoke()
            }

            content()
        }
    }
}

/** True when at least one property exists that we can meaningfully compare against Earth. */
internal fun Exoplanet.hasEarthComparisonData(): Boolean =
    planetRadiusEarth != null || planetMassEarth != null ||
            equilibriumTempK != null || orbitalPeriodDays != null || insolationFlux != null

@Composable
internal fun EarthComparisonCard(planet: Exoplanet) {
    DetailCard(
        title = stringResource(R.string.planet_detail_title_earth_comparison),
        icon = Icons.Default.Public,
        iconColor = CosmicCyan,
    ) {
        Text(
            text = stringResource(R.string.planet_detail_earth_comparison_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        planet.planetRadiusEarth?.let { r ->
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_radius),
                ratio = r,
                valueText = "${String.format("%.2f", r)} R⊕",
            )
        }
        planet.planetMassEarth?.let { m ->
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_mass),
                ratio = m,
                valueText = "${String.format("%.2f", m)} M⊕",
            )
        }
        // Surface gravity (estimated): g ∝ M / R² in Earth units
        if (planet.planetMassEarth != null && planet.planetRadiusEarth != null && planet.planetRadiusEarth!! > 0.0) {
            val g =
                planet.planetMassEarth!! / (planet.planetRadiusEarth!! * planet.planetRadiusEarth!!)
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_surface_gravity_est),
                ratio = g,
                valueText = "${String.format("%.2f", g)} g",
            )
        }
        planet.equilibriumTempK?.let { t ->
            // Earth's equilibrium temperature ≈ 255 K
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_eq_temperature_lower),
                ratio = t / 255.0,
                valueText = "${t.toInt()} K",
            )
        }
        planet.orbitalPeriodDays?.let { p ->
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_orbital_period),
                ratio = p / 365.25,
                valueText = "${String.format("%.1f", p)} d",
            )
        }
        planet.insolationFlux?.let { s ->
            // Insolation flux is already expressed in Earth units
            EarthComparisonRow(
                stringResource(R.string.planet_detail_label_insolation),
                ratio = s,
                valueText = "${String.format("%.2f", s)} S⊕",
            )
        }
    }
}

@Composable
private fun EarthComparisonRow(label: String, ratio: Double, valueText: String) {
    // Color by closeness to Earth: similar = green, larger = cyan, smaller = pink
    val barColor = when {
        ratio in 0.5..2.0 -> AuroraGreen
        ratio > 2.0 -> CosmicCyan
        else -> NebulaPink
    }
    // Map ratio to bar fill on a log scale: 0.01× → 0, 1× (Earth) → 0.5, 100× → 1.0
    val fill = if (ratio > 0.0) {
        (0.5 + 0.5 * (log10(ratio) / 2.0)).coerceIn(0.02, 1.0).toFloat()
    } else 0.02f
    val multiplierText = if (ratio >= 1.0) "${String.format("%.1f", ratio)}× Earth"
    else "${String.format("%.2f", ratio)}× Earth"

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(valueText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    multiplierText,
                    style = MaterialTheme.typography.labelMedium,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardLight),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor),
            )
            // Earth baseline marker at the 1× midpoint
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.White.copy(alpha = 0.7f)),
            )
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
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = CosmicCyan,
                fontSize = 10.sp,
            )
        }
    }
}
