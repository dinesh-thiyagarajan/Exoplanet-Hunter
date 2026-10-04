package com.app.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.JUPITER_RADIUS_EARTH
import com.app.exoplanethunter.presentation.components.NEPTUNE_RADIUS_EARTH
import com.app.exoplanethunter.presentation.components.RealisticPlanet
import com.app.exoplanethunter.presentation.components.bulkDensity
import com.app.exoplanethunter.presentation.components.composition
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.graticule
import com.app.exoplanethunter.presentation.components.starLightColor
import com.app.exoplanethunter.presentation.components.surfaceGravity
import com.app.exoplanethunter.presentation.components.surfaceKind
import com.app.exoplanethunter.presentation.components.swatch
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import kotlin.math.max

/**
 * The detail screen's hero: the planet rendered as it most plausibly looks, lit by its own
 * star, with the four numbers that say most about living there pinned to the corners.
 */
@Composable
internal fun PlanetHero(planet: Exoplanet, tidallyLocked: Boolean, modifier: Modifier = Modifier) {
    val starColor = remember(planet) { starLightColor(planet.stellarEffectiveTempK) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .graticule()
            // The host star's glow, off-frame where the light comes from.
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(starColor.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset.Zero,
                        radius = size.minDimension * 0.75f,
                    ),
                    radius = size.minDimension * 0.75f,
                    center = Offset.Zero,
                )
            },
    ) {
        RealisticPlanet(
            planet = planet,
            size = 300.dp,
            tidallyLocked = tidallyLocked,
            modifier = Modifier.align(Alignment.Center),
        )

        val temp = planet.estimatedTempK()
        Readout(
            label = stringResource(
                if (planet.equilibriumTempK != null) R.string.hero_label_temp else R.string.hero_label_est_temp
            ),
            value = temp?.let { "%,d°C".format((it - 273.15).toInt()) },
            detail = temp?.let { "%,d K".format(it.toInt()) },
            modifier = Modifier.align(Alignment.TopStart),
        )
        Readout(
            label = stringResource(R.string.hero_label_gravity),
            value = planet.surfaceGravity()?.let { "%.2f g".format(it) },
            detail = stringResource(R.string.hero_detail_earth_is_one_g),
            end = true,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        Readout(
            label = stringResource(R.string.hero_label_year),
            value = planet.orbitalPeriodDays?.let { formatYear(it) },
            detail = planet.orbitSemiMajorAxisAu?.let { "%.3f AU".format(it) },
            modifier = Modifier.align(Alignment.BottomStart),
        )
        Readout(
            label = stringResource(R.string.hero_label_density),
            value = planet.bulkDensity()?.let { "%.1f g/cm³".format(it) },
            detail = stringResource(R.string.hero_detail_earth_density),
            end = true,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

/** "19.6 h", "32.9 days", "11.9 yr" — whichever unit reads most naturally. */
private fun formatYear(days: Double): String = when {
    days < 1.0 -> "%.1f h".format(days * 24)
    days < 100.0 -> "%.1f days".format(days)
    days < 730.0 -> "%.0f days".format(days)
    else -> "%.1f yr".format(days / 365.25)
}

@Composable
private fun Readout(
    label: String,
    value: String?,
    detail: String?,
    modifier: Modifier = Modifier,
    end: Boolean = false,
) {
    Column(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = if (end) Alignment.End else Alignment.Start,
    ) {
        Text(label, style = AlmanacMeta.copy(color = Brass))
        Text(value ?: "—", style = AlmanacData.copy(fontSize = 17.sp))
        if (value != null && detail != null) Text(detail, style = AlmanacMeta.copy(fontSize = 10.sp))
    }
}

/**
 * "Water-rich world" plus how that was worked out — and a reminder the picture is an
 * informed impression, not a photograph.
 */
@Composable
internal fun CompositionLine(planet: Exoplanet, modifier: Modifier = Modifier) {
    val (composition, basis) = remember(planet) { planet.composition() }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(composition.labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = Brass,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.hero_impression_caption, stringResource(basis.labelRes)),
            style = AlmanacMeta,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/**
 * The planet drawn to scale beside Earth — and Neptune and Jupiter once it's big enough
 * for them to be the useful yardstick.
 */
@Composable
internal fun SizeToScaleCard(planet: Exoplanet, modifier: Modifier = Modifier) {
    val radius = planet.planetRadiusEarth?.takeIf { it > 0.0 } ?: return
    val swatch = remember(planet) { planet.surfaceKind().swatch() }
    val bodies = buildList {
        add(ScaleBody(stringResource(R.string.size_scale_earth), 1.0, Color(0xFF3F7FB5)))
        if (radius > 2.5) add(ScaleBody(stringResource(R.string.size_scale_neptune), NEPTUNE_RADIUS_EARTH, Color(0xFF4A7FD0)))
        if (radius > 6.0) add(ScaleBody(stringResource(R.string.size_scale_jupiter), JUPITER_RADIUS_EARTH, Color(0xFFD2B48C)))
        add(ScaleBody(planet.planetName, radius, swatch, highlight = true))
    }.sortedBy { it.radiusEarth }
    val largest = bodies.maxOf { it.radiusEarth }
    val maxDiameter = 84.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Hairline, RoundedCornerShape(8.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.size_scale_title), style = AlmanacSectionLabel, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.size_scale_ratio, "%.2f".format(radius)),
                style = AlmanacMeta.copy(color = Brass),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            bodies.forEach { body ->
                val d: Dp = max(3f, (maxDiameter.value * body.radiusEarth / largest).toFloat()).dp
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(d)
                            .clip(CircleShape)
                            .drawBehind {
                                // Lit from the upper left, like the hero planet.
                                drawCircle(
                                    Brush.radialGradient(
                                        listOf(body.color, body.color.copy(alpha = 0.45f)),
                                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                                        radius = size.width * 1.1f,
                                    )
                                )
                            }
                            .then(if (body.highlight) Modifier.border(1.dp, Brass, CircleShape) else Modifier),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body.label,
                        style = AlmanacMeta.copy(color = if (body.highlight) Brass else InkTextFaint),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private data class ScaleBody(
    val label: String,
    val radiusEarth: Double,
    val color: Color,
    val highlight: Boolean = false,
)

@Preview
@Composable
private fun PlanetHeroPreview() = PreviewSurface {
    Column {
        PlanetHero(planet = PreviewData.planet, tidallyLocked = false)
        CompositionLine(planet = PreviewData.planet, modifier = Modifier.fillMaxWidth())
        SizeToScaleCard(planet = PreviewData.planet, modifier = Modifier.padding(20.dp))
    }
}
