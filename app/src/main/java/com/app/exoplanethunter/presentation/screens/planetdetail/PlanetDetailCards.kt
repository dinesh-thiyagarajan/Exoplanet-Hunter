package com.app.exoplanethunter.presentation.screens.planetdetail

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.presentation.components.AboutEstimatesDialog
import com.app.exoplanethunter.presentation.components.AiChip
import com.app.exoplanethunter.presentation.components.DashedCard
import com.app.exoplanethunter.presentation.components.SecondaryButton
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.StarGlyph
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.TempIndicator
import com.app.exoplanethunter.presentation.components.agreesWith
import com.app.exoplanethunter.presentation.components.bulkDensity
import com.app.exoplanethunter.presentation.components.composition
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.components.isLikelyTidallyLocked
import com.app.exoplanethunter.presentation.components.starClassColor
import com.app.exoplanethunter.presentation.components.surfaceGravity
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.HeroFigure
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.SectionTitle
import com.app.exoplanethunter.presentation.theme.Surface
import kotlin.math.log10

// ===========================================================================
// Planet detail sections, top to bottom.
// ===========================================================================

private const val EARTH_EQ_TEMP_K = 255.0

/** "≈ " prefix for estimated values. */
internal const val APPROX = "≈ "

/** Whether the temperature shown is an estimate rather than a catalogue measurement. */
internal fun Exoplanet.tempIsEstimated(): Boolean = equilibriumTempK == null && estimatedTempK() != null

/**
 * One plain sentence on what the world is probably like, e.g. "A temperate water-rich world,
 * 2.6× wider than Earth, in the zone around its star where water could stay liquid."
 */
internal fun planetSummary(context: Context, planet: Exoplanet): String {
    val bucket = TempBucket.of(planet.estimatedTempK())
    val adjective = when (bucket) {
        TempBucket.Scorched -> context.getString(R.string.summary_adj_scorched)
        TempBucket.Hot -> context.getString(R.string.summary_adj_hot)
        TempBucket.Temperate -> context.getString(R.string.summary_adj_temperate)
        TempBucket.Cold -> context.getString(R.string.summary_adj_cold)
        TempBucket.Frozen -> context.getString(R.string.summary_adj_frozen)
        TempBucket.Unknown -> null
    }
    val noun = context.getString(planet.composition().first.labelRes).replaceFirstChar { it.lowercase() }
    val head = listOfNotNull(adjective, noun).joinToString(" ")
    val article = if (head.first().lowercaseChar() in "aeiou") "An" else "A"
    val size = planet.planetRadiusEarth?.let { r ->
        when {
            r >= 1.15 -> context.getString(R.string.summary_wider, "%.1f".format(r))
            r <= 0.87 -> context.getString(R.string.summary_smaller, "%.0f".format((1 - r) * 100))
            else -> context.getString(R.string.summary_earth_size)
        }
    }
    val zone = when (bucket) {
        TempBucket.Temperate -> context.getString(R.string.summary_zone_temperate)
        TempBucket.Hot, TempBucket.Scorched -> context.getString(R.string.summary_zone_hot)
        TempBucket.Cold, TempBucket.Frozen -> context.getString(R.string.summary_zone_cold)
        TempBucket.Unknown -> null
    }
    return listOfNotNull("$article $head", size, zone).joinToString(", ") + "."
}

/** The "what is it like" card: temperature word, AI type and composition, and one sentence. */
@Composable
internal fun PlanetSummaryCard(planet: Exoplanet, insight: HabitabilityInsight?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bucket = TempBucket.of(planet.estimatedTempK())
    val composition = planet.composition().first
    val tidallyLocked = remember(planet) { isLikelyTidallyLocked(planet) }
    SectionCard(modifier = modifier, small = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TempIndicator(bucket, fontSize = 13)
            // The AI type is shown only when it doesn't contradict the composition.
            insight?.takeIf { it.classification.agreesWith(composition) }?.let { AiChip() }
            Text(stringResource(composition.labelRes), style = AlmanacMeta.copy(color = InkTextDim, fontSize = 13.sp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            planetSummary(context, planet),
            fontFamily = BodyFamily,
            fontSize = 17.sp,
            lineHeight = 25.sp,
            color = InkText,
        )
        if (tidallyLocked) {
            Spacer(Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = InkTextDim)) {
                        append(stringResource(R.string.tidal_lock_badge))
                    }
                    append(" · ")
                    append(stringResource(R.string.tidal_lock_desc))
                },
                style = AlmanacMeta,
            )
        }
    }
}

/** The 2×2 grid of hero figures: temperature, gravity, year, density. */
@Composable
internal fun HeroFigureGrid(planet: Exoplanet, modifier: Modifier = Modifier) {
    val temp = planet.estimatedTempK()
    val tempEstimated = planet.tempIsEstimated()
    val anyEstimated = tempEstimated
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroFigureCard(
                label = stringResource(if (tempEstimated) R.string.hero_label_est_temp else R.string.hero_label_temp),
                value = temp?.let { "%,d K".format(it.toInt()) },
                estimated = tempEstimated,
                detail = temp?.let { stringResource(R.string.hero_detail_celsius, "%,d".format((it - 273.15).toInt())) },
                modifier = Modifier.weight(1f),
            )
            HeroFigureCard(
                label = stringResource(R.string.hero_label_gravity),
                value = planet.surfaceGravity()?.let { "%.1f g".format(it) },
                detail = stringResource(R.string.hero_detail_earth_is_one_g),
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroFigureCard(
                label = stringResource(R.string.hero_label_year),
                value = planet.orbitalPeriodDays?.let { formatOrbitalPeriod(it) },
                detail = planet.orbitalPeriodDays?.let { stringResource(R.string.hero_detail_measured) },
                modifier = Modifier.weight(1f),
            )
            HeroFigureCard(
                label = stringResource(R.string.hero_label_density),
                value = planet.bulkDensity()?.let { "%.1f".format(it) },
                detail = stringResource(R.string.hero_detail_earth_density),
                modifier = Modifier.weight(1f),
            )
        }
        if (anyEstimated) {
            Text(stringResource(R.string.estimate_footnote), style = AlmanacCaption, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun HeroFigureCard(label: String, value: String?, detail: String?, modifier: Modifier = Modifier, estimated: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(Corners.SmallCard)
            .background(Surface)
            .padding(16.dp),
    ) {
        Text(label, style = AlmanacSectionLabel)
        Spacer(Modifier.height(4.dp))
        Text(
            buildAnnotatedString {
                if (value != null && estimated) withStyle(SpanStyle(color = InkTextFaint)) { append(APPROX) }
                append(value ?: "—")
            },
            style = HeroFigure,
        )
        if (value != null && detail != null) {
            Spacer(Modifier.height(4.dp))
            Text(detail, style = AlmanacCaption)
        }
    }
}

/** Section heading in Caprasimo, with an optional trailing element. */
@Composable
internal fun SectionHeading(title: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = SectionTitle, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** AI habitability: a ring gauge with the score, the verdict, and "About these estimates". */
@Composable
internal fun HabitabilityCard(planet: Exoplanet, insight: HabitabilityInsight) {
    var showDisclaimer by remember { mutableStateOf(false) }
    if (showDisclaimer) AboutEstimatesDialog(onDismiss = { showDisclaimer = false })

    val reliable = insight.habitabilityReliable
    val score = (insight.overallScore * 100).toInt().coerceIn(0, 100)
    val verdict = when {
        !reliable -> stringResource(R.string.planet_detail_hab_desc_insufficient)
        insight.overallScore > 0.7 -> stringResource(R.string.planet_detail_hab_desc_high)
        insight.overallScore > 0.4 -> stringResource(R.string.planet_detail_hab_desc_moderate)
        insight.overallScore > 0.2 -> stringResource(R.string.planet_detail_hab_desc_low)
        else -> stringResource(R.string.planet_detail_hab_desc_hostile)
    }
    val esi = earthSimilarity(planet)
    val composition = planet.composition().first

    SectionCard {
        SectionHeading(stringResource(R.string.planet_detail_habitability_title)) { AiChip(label = stringResource(R.string.ai_estimate)) }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RingGauge(fraction = if (reliable) score / 100f else 0f, size = 104) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (reliable) "$score" else "—", style = HeroFigure.copy(fontSize = 30.sp))
                    if (reliable) Text(stringResource(R.string.of_100), style = AlmanacCaption)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(verdict, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = InkText)
                Spacer(Modifier.height(6.dp))
                esi?.let {
                    Text(stringResource(R.string.planet_detail_earth_similarity_value, "%.2f".format(it)), style = AlmanacMeta.copy(color = InkTextDim, fontSize = 14.sp))
                }
                if (insight.classification.agreesWith(composition)) {
                    Text(
                        stringResource(
                            R.string.planet_detail_ai_type,
                            insight.classification.label,
                            stringResource(if (reliable) R.string.confidence_high else R.string.confidence_low),
                        ),
                        style = AlmanacMeta.copy(color = InkTextDim, fontSize = 14.sp),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.planet_detail_disclaimer_link),
            fontFamily = BodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Matcha,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clip(Corners.Pill).clickable { showDisclaimer = true }.padding(vertical = 6.dp),
        )
    }
}

/** A ring gauge: a raised track with a matcha arc for [fraction], and [content] in the middle. */
@Composable
internal fun RingGauge(fraction: Float, size: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size.dp)) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(Raised, 0f, 360f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke))
            drawArc(Matcha, -90f, 360f * fraction, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        content()
    }
}

/**
 * Earth Similarity Index (0–1) from planet radius and equilibrium temperature — a
 * transparent, open formula (distinct from the AI habitability verdict). Null when neither
 * input is known.
 */
internal fun earthSimilarity(planet: Exoplanet): Double? {
    val r = planet.planetRadiusEarth
    val t = planet.equilibriumTempK
    val terms = mutableListOf<Double>()
    if (r != null && r > 0) terms.add(1.0 - kotlin.math.abs(r - 1.0) / (r + 1.0))
    if (t != null && t > 0) terms.add(1.0 - kotlin.math.abs(t - EARTH_EQ_TEMP_K) / (t + EARTH_EQ_TEMP_K))
    if (terms.isEmpty()) return null
    // Geometric mean of the available similarity terms.
    val product = terms.fold(1.0) { acc, v -> acc * v }
    return Math.pow(product, 1.0 / terms.size).coerceIn(0.0, 1.0)
}

/** True when at least one property exists that we can meaningfully compare against Earth. */
internal fun Exoplanet.hasEarthComparisonData(): Boolean =
    planetRadiusEarth != null || planetMassEarth != null ||
        estimatedTempK() != null || orbitalPeriodDays != null || insolationFlux != null

/** "Compared to Earth": log-scale bars with a white Earth tick at the centre. */
@Composable
internal fun EarthComparisonSection(planet: Exoplanet) {
    Column {
        SectionHeading(stringResource(R.string.planet_detail_title_earth_comparison))
        Spacer(Modifier.height(12.dp))
        planet.planetRadiusEarth?.let { EarthBar(stringResource(R.string.planet_detail_label_radius), it) }
        planet.planetMassEarth?.let { EarthBar(stringResource(R.string.planet_detail_label_mass), it) }
        planet.surfaceGravity()?.let { EarthBar(stringResource(R.string.hero_label_gravity), it) }
        planet.estimatedTempK()?.let {
            EarthBar(stringResource(R.string.compare_metric_eq_temp), it / EARTH_EQ_TEMP_K, estimated = planet.tempIsEstimated())
        }
        planet.orbitalPeriodDays?.let { EarthBar(stringResource(R.string.planet_detail_label_orbital_period), it / 365.25) }
        planet.insolationFlux?.let { EarthBar(stringResource(R.string.compare_metric_insolation), it) }
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.planet_detail_earth_comparison_desc), style = AlmanacCaption)
    }
}

/** Formats a ratio to Earth, e.g. "2.6×", "0.09×". */
internal fun ratioText(ratio: Double): String = when {
    ratio >= 10 -> "%,.0f×".format(ratio)
    ratio >= 1 -> "%.1f×".format(ratio)
    ratio >= 0.1 -> "%.1f×".format(ratio)
    else -> "%.2f×".format(ratio)
}

@Composable
private fun EarthBar(label: String, ratio: Double, estimated: Boolean = false) {
    // Log scale: 0.01× → 0, 1× (Earth) → 0.5, 100× → 1.
    val fill = if (ratio > 0.0) (0.5 + 0.5 * (log10(ratio) / 2.0)).coerceIn(0.03, 1.0).toFloat() else 0.03f
    Column(Modifier.padding(vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim, modifier = Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    if (estimated) withStyle(SpanStyle(color = InkTextFaint)) { append(APPROX) }
                    append(ratioText(ratio))
                },
                style = AlmanacData.copy(fontSize = 14.sp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(16.dp)) {
            val h = 8.dp.toPx()
            val y = (size.height - h) / 2
            val r = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
            drawRoundRect(Raised, Offset(0f, y), androidx.compose.ui.geometry.Size(size.width, h), r)
            drawRoundRect(Matcha, Offset(0f, y), androidx.compose.ui.geometry.Size(size.width * fill, h), r)
            // Earth (1×) tick at the centre.
            drawLine(InkText, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

/** A plain-English label for a star's spectral class, e.g. "red dwarf". */
@Composable
private fun starKindLabel(spectralType: String?): String? = when (spectralType?.trim()?.firstOrNull()?.uppercaseChar()) {
    'M' -> stringResource(R.string.star_kind_m)
    'K' -> stringResource(R.string.star_kind_k)
    'G' -> stringResource(R.string.star_kind_g)
    'F' -> stringResource(R.string.star_kind_f)
    'A', 'B', 'O' -> stringResource(R.string.star_kind_hot)
    else -> null
}

/** Host star: the star, its kind, a few facts, and a button into its system. */
@Composable
internal fun HostStarCard(planet: Exoplanet, onOpenSystem: (() -> Unit)?) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarGlyph(color = starClassColor(planet.spectralType), size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(planet.hostName, style = CardTitle)
                Text(
                    listOfNotNull(stringResource(R.string.host_star), starKindLabel(planet.spectralType), planet.spectralType?.takeIf { it.isNotBlank() })
                        .joinToString(" · "),
                    style = AlmanacMeta,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        planet.stellarEffectiveTempK?.let { DataRow(stringResource(R.string.planet_detail_label_temperature), "%,d K".format(it.toInt())) }
        planet.stellarRadiusSolar?.let { DataRow(stringResource(R.string.planet_detail_label_radius), "%.2f R☉".format(it)) }
        planet.stellarMassSolar?.let { DataRow(stringResource(R.string.planet_detail_label_mass), "%.2f M☉".format(it)) }
        planet.stellarMetallicity?.let { DataRow(stringResource(R.string.planet_detail_label_metallicity), "%.2f".format(it), "[Fe/H]") }
        planet.stellarSurfaceGravity?.let { DataRow(stringResource(R.string.planet_detail_label_surface_gravity), "%.2f".format(it), "log g") }
        DataRow(
            stringResource(R.string.star_system_in_system_label),
            pluralStringResource(R.plurals.stars_count, planet.numStars.coerceAtLeast(1), planet.numStars.coerceAtLeast(1)) +
                " · " + pluralStringResource(R.plurals.planet_count, planet.numPlanets, planet.numPlanets),
        )
        if (onOpenSystem != null) {
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                label = stringResource(R.string.open_star_system),
                onClick = onOpenSystem,
                filled = true,
                trailingIcon = AppIcons.ArrowRight,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** A label / value row inside a card, with an optional unit line under the label. */
@Composable
internal fun DataRow(label: String, value: String, unit: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim)
            if (unit != null) Text(unit, style = AlmanacCaption)
        }
        Text(value, style = AlmanacData)
    }
}

/** Planet properties as a plain list: label, unit line, value. */
@Composable
internal fun PropertiesSection(planet: Exoplanet) {
    Column {
        SectionHeading(stringResource(R.string.planet_detail_title_properties))
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 4.dp)) {
            planet.planetRadiusEarth?.let {
                DataRow(stringResource(R.string.planet_detail_label_radius), "%.2f".format(it), stringResource(R.string.planet_detail_subtitle_earth_radii))
            }
            if (planet.planetMassEarth != null || planet.planetMassJupiter != null) {
                DataRow(
                    stringResource(R.string.planet_detail_label_mass),
                    listOfNotNull(planet.planetMassEarth?.let { "%.2f".format(it) }, planet.planetMassJupiter?.let { "%.3f".format(it) }).joinToString(" · "),
                    listOfNotNull(
                        planet.planetMassEarth?.let { stringResource(R.string.planet_detail_subtitle_earth_masses) },
                        planet.planetMassJupiter?.let { stringResource(R.string.planet_detail_subtitle_jupiter_masses) },
                    ).joinToString(" · "),
                )
            }
            planet.orbitalPeriodDays?.let {
                DataRow(stringResource(R.string.planet_detail_label_orbit_period), "%.2f".format(it), stringResource(R.string.unit_days))
            }
            planet.orbitSemiMajorAxisAu?.let {
                DataRow(stringResource(R.string.planet_detail_label_semi_major_axis), "%.3f".format(it), "AU")
            }
            planet.eccentricity?.let { DataRow(stringResource(R.string.planet_detail_label_eccentricity), "%.2f".format(it)) }
            planet.estimatedTempK()?.let {
                val est = planet.tempIsEstimated()
                DataRow(
                    stringResource(R.string.planet_detail_label_eq_temperature),
                    (if (est) APPROX else "") + "%,d".format(it.toInt()),
                    stringResource(if (est) R.string.unit_kelvin_estimated else R.string.unit_kelvin),
                )
            }
            planet.insolationFlux?.let {
                DataRow(stringResource(R.string.planet_detail_label_insolation), "%.2f".format(it), stringResource(R.string.unit_earth_is_one))
            }
        }
    }
}

/** Discovery facts: method, year, facility, distance, coordinates. */
@Composable
internal fun DiscoverySection(planet: Exoplanet) {
    Column {
        SectionHeading(stringResource(R.string.planet_detail_title_discovery))
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 4.dp)) {
            DataRow(stringResource(R.string.planet_detail_label_method), planet.discoveryMethod)
            DataRow(stringResource(R.string.planet_detail_label_year), planet.discoveryYear.toString())
            if (planet.discoveryFacility.isNotBlank()) DataRow(stringResource(R.string.planet_detail_label_facility), planet.discoveryFacility)
            planet.distanceParsec?.let {
                DataRow(stringResource(R.string.planet_detail_label_distance), "%,.1f ly".format(it * 3.26156), "%,.2f parsecs".format(it))
            }
        }
    }
}

/** AI insights in a dashed card, with the disclaimer under them. */
@Composable
internal fun InsightsCard(insights: List<String>) {
    DashedCard {
        AiChip(label = stringResource(R.string.planet_detail_title_insights))
        Spacer(Modifier.height(12.dp))
        Text(insights.joinToString(" "), fontFamily = BodyFamily, fontSize = 16.sp, lineHeight = 24.sp, color = InkText)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.ai_disclaimer_short), style = AlmanacCaption)
    }
}

/** A placeholder for the ad slot while ads are switched off. */
@Composable
internal fun DashedBox(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(InkTextFaint, Offset.Zero, Offset(size.width, 0f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            }
    )
}

@Preview
@Composable
private fun PlanetSummaryCardPreview() = PreviewSurface {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PlanetSummaryCard(PreviewData.planet, PreviewData.insight)
        HeroFigureGrid(PreviewData.planet)
        HabitabilityCard(PreviewData.planet, PreviewData.insight)
    }
}
