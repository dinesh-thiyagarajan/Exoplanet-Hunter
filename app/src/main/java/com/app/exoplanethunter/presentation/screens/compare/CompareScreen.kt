package com.app.exoplanethunter.presentation.screens.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.app.exoplanethunter.presentation.components.RealisticPlanet
import com.app.exoplanethunter.presentation.components.SizeToScaleCard
import com.app.exoplanethunter.presentation.components.agreesWith
import com.app.exoplanethunter.presentation.components.bulkDensity
import com.app.exoplanethunter.presentation.components.composition
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatLightYears
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.components.isLikelyTidallyLocked
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.starTypeLabel
import com.app.exoplanethunter.presentation.components.surfaceGravity
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.CautionYellow
import com.app.exoplanethunter.presentation.theme.HabitableGreen
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.HostileRed
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

@Composable
fun CompareScreen(
    planetAId: Long,
    planetBId: Long,
    onBack: () -> Unit,
    onPlanetClick: (Long) -> Unit,
    viewModel: CompareViewModel = koinViewModel()
) {
    LaunchedEffect(planetAId, planetBId) {
        viewModel.load(planetAId, planetBId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceBlack.copy(alpha = 0.85f))
                    .topBarInsets()
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = InkText
                    )
                }
                Text(
                    text = stringResource(R.string.compare_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = InkText
                )
            }

            val a = viewModel.planetA
            val b = viewModel.planetB

            if (viewModel.isLoading || a == null || b == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (viewModel.isLoading) CircularProgressIndicator(color = Brass)
                }
                return@Column
            }

            val insightA = viewModel.insightA
            val insightB = viewModel.insightB

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .screenContentInsets()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // The two worlds, rendered as they most plausibly look
                Row(modifier = Modifier.fillMaxWidth()) {
                    PlanetHeader(planet = a, insight = insightA, modifier = Modifier.weight(1f), onClick = { onPlanetClick(a.id) })
                    PlanetHeader(planet = b, insight = insightB, modifier = Modifier.weight(1f), onClick = { onPlanetClick(b.id) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plain-language summary of the most meaningful differences
                VerdictBanner(a, b, insightA, insightB)

                Spacer(modifier = Modifier.height(16.dp))

                SizeToScaleCard(planets = listOf(a, b))

                Spacer(modifier = Modifier.height(24.dp))

                // ── Physical properties ──
                SectionHeader(stringResource(R.string.compare_section_properties))
                ColumnNames(a, b)
                metricsFor(a, b).forEach { MetricRow(it) }
                if (a.equilibriumTempK == null && a.estimatedTempK() != null ||
                    b.equilibriumTempK == null && b.estimatedTempK() != null
                ) {
                    Text(
                        text = stringResource(R.string.compare_estimated_footnote),
                        style = AlmanacMeta,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── AI habitability estimate ──
                SectionHeader(stringResource(R.string.compare_section_ml))
                ColumnNames(a, b)
                val bothScored = insightA?.habitabilityReliable == true || insightB?.habitabilityReliable == true
                if (bothScored) {
                    ScoreRow(
                        label = stringResource(R.string.compare_overall_habitability),
                        scoreA = insightA?.overallScore?.takeIf { insightA.habitabilityReliable },
                        scoreB = insightB?.overallScore?.takeIf { insightB.habitabilityReliable },
                    )
                }
                categoryLabels(insightA, insightB).forEach { label ->
                    ScoreRow(
                        label = label,
                        scoreA = insightA?.scores?.get(label),
                        scoreB = insightB?.scores?.get(label)
                    )
                }
                Text(
                    text = stringResource(R.string.compare_ml_disclaimer),
                    style = AlmanacMeta,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = AlmanacSectionLabel,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun PlanetHeader(
    planet: Exoplanet,
    insight: HabitabilityInsight?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val composition = remember(planet) { planet.composition().first }
    // The AI's type only when it doesn't contradict the density/radius reading.
    val aiType = insight?.classification?.takeIf { it.agreesWith(composition) && it != PlanetClassification.UNKNOWN }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RealisticPlanet(
            planet = planet,
            size = 150.dp,
            tidallyLocked = remember(planet) { isLikelyTidallyLocked(planet) },
        )
        Text(
            text = planet.planetName,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.planet_detail_subtitle, planet.hostName),
            style = AlmanacCaption,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(composition.labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = Brass,
            textAlign = TextAlign.Center,
        )
        aiType?.let {
            Text(
                text = stringResource(R.string.compare_ai_type, it.label),
                style = AlmanacMeta,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Verdict
// ---------------------------------------------------------------------------

@Composable
private fun VerdictBanner(
    a: Exoplanet,
    b: Exoplanet,
    insightA: HabitabilityInsight?,
    insightB: HabitabilityInsight?
) {
    val headline = earthLikenessHeadline(a, b, insightA, insightB)
    val supporting = listOfNotNull(
        sizeVerdict(a, b),
        distanceVerdict(a, b),
        temperatureVerdict(a, b)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Hairline, RoundedCornerShape(8.dp))
            .background(Brass.copy(alpha = 0.06f))
            .padding(14.dp)
    ) {
        Text(
            text = stringResource(R.string.compare_verdict_title).uppercase(),
            style = AlmanacSectionLabel.copy(color = Brass)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.titleMedium,
            color = InkText
        )
        supporting.forEach { line ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "·  $line",
                style = MaterialTheme.typography.bodyMedium,
                color = InkTextDim
            )
        }
    }
}

@Composable
private fun earthLikenessHeadline(
    a: Exoplanet,
    b: Exoplanet,
    insightA: HabitabilityInsight?,
    insightB: HabitabilityInsight?
): String {
    val aReliable = insightA?.habitabilityReliable == true
    val bReliable = insightB?.habitabilityReliable == true

    // Prefer the AI habitability score when both are reliable, else fall back to a
    // radius/temperature closeness heuristic; lower penalty = more Earth-like.
    val (scoreA, scoreB, higherIsBetter) = if (aReliable && bReliable) {
        Triple(insightA!!.overallScore, insightB!!.overallScore, true)
    } else {
        Triple(earthPenalty(a), earthPenalty(b), false)
    }

    if (scoreA == null || scoreB == null) {
        return stringResource(R.string.compare_verdict_unknown_earthlike)
    }
    if (abs(scoreA - scoreB) < 0.05) {
        return stringResource(R.string.compare_verdict_similar_earthlike)
    }
    val aMoreEarthLike = if (higherIsBetter) scoreA > scoreB else scoreA < scoreB
    val winner = if (aMoreEarthLike) a.planetName else b.planetName
    return stringResource(R.string.compare_verdict_more_earthlike, winner)
}

@Composable
private fun sizeVerdict(a: Exoplanet, b: Exoplanet): String? {
    val ra = a.planetRadiusEarth
    val rb = b.planetRadiusEarth
    if (ra == null || rb == null || ra <= 0.0 || rb <= 0.0) return null
    val factor = max(ra, rb) / min(ra, rb)
    if (factor < 1.1) return stringResource(R.string.compare_verdict_same_size)
    val bigger = if (ra >= rb) a.planetName else b.planetName
    return stringResource(R.string.compare_verdict_larger, bigger, String.format("%.1f", factor))
}

@Composable
private fun distanceVerdict(a: Exoplanet, b: Exoplanet): String? {
    val da = a.distanceParsec
    val db = b.distanceParsec
    if (da == null || db == null) return null
    val closerName = if (da <= db) a.planetName else b.planetName
    val closerLy = min(da, db) * 3.26156
    val fartherLy = max(da, db) * 3.26156
    return stringResource(
        R.string.compare_verdict_closer,
        closerName,
        String.format("%.1f", closerLy),
        String.format("%.1f", fartherLy)
    )
}

@Composable
private fun temperatureVerdict(a: Exoplanet, b: Exoplanet): String? {
    val ta = a.estimatedTempK()
    val tb = b.estimatedTempK()
    if (ta == null || tb == null || abs(ta - tb) < 1.0) return null
    val hotter = if (ta >= tb) a.planetName else b.planetName
    return stringResource(R.string.compare_verdict_hotter, hotter)
}

/** Radius/temperature penalty vs Earth (lower = closer to Earth); null when unknown. */
private fun earthPenalty(planet: Exoplanet): Double? {
    val r = planet.planetRadiusEarth
    val t = planet.estimatedTempK()
    if (r == null && t == null) return null
    var penalty = 0.0
    if (r != null && r > 0.0) penalty += abs(log10(r))
    if (t != null) penalty += abs(t - 255.0) / 255.0
    return penalty
}

// ---------------------------------------------------------------------------
// Side-by-side rows
// ---------------------------------------------------------------------------

/** Planet names over the left and right columns, so long tables stay readable. */
@Composable
private fun ColumnNames(a: Exoplanet, b: Exoplanet) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            a.planetName, style = AlmanacMeta.copy(color = Brass), maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            b.planetName, style = AlmanacMeta.copy(color = Brass), maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f)
        )
    }
}

/**
 * One property, both planets: values at the edges, the label (and how many times larger
 * one is, when that's meaningful) in the middle. Neither side is highlighted as "better" —
 * bigger isn't better for most of these.
 */
private data class CompareMetric(
    val label: String,
    val displayA: String,
    val displayB: String,
    val ratio: String? = null,
)

@Composable
private fun MetricRow(metric: CompareMetric) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Hairline))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(metric.displayA, style = AlmanacData, modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(metric.label, style = AlmanacMeta, textAlign = TextAlign.Center)
                metric.ratio?.let { Text(it, style = AlmanacMeta.copy(color = Brass)) }
            }
            Text(metric.displayB, style = AlmanacData, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun metricsFor(a: Exoplanet, b: Exoplanet): List<CompareMetric> {
    val none = stringResource(R.string.compare_no_data)
    fun Double?.or(format: (Double) -> String) = this?.let(format) ?: none

    @Composable
    fun ratioOf(x: Double?, y: Double?): String? {
        if (x == null || y == null || x <= 0.0 || y <= 0.0) return null
        val factor = max(x, y) / min(x, y)
        return if (factor >= 1.1) stringResource(R.string.compare_ratio, "%.1f".format(factor)) else null
    }

    fun temp(p: Exoplanet): String {
        val t = p.estimatedTempK() ?: return none
        val c = "%,d°C".format((t - 273.15).toInt())
        return if (p.equilibriumTempK == null) "≈ $c" else c
    }

    fun star(p: Exoplanet): String {
        val type = starTypeLabel(p.spectralType).takeIf { it != "UNCLASSED" }
        val temp = p.stellarEffectiveTempK?.let { "%,d K".format(it.toInt()) }
        return listOfNotNull(type, temp).joinToString(" · ").ifEmpty { none }
    }

    return listOf(
        CompareMetric(
            stringResource(R.string.compare_metric_radius),
            a.planetRadiusEarth.or { "%.2f R⊕".format(it) }, b.planetRadiusEarth.or { "%.2f R⊕".format(it) },
            ratioOf(a.planetRadiusEarth, b.planetRadiusEarth)
        ),
        CompareMetric(
            stringResource(R.string.compare_metric_mass),
            a.planetMassEarth.or { "%.2f M⊕".format(it) }, b.planetMassEarth.or { "%.2f M⊕".format(it) },
            ratioOf(a.planetMassEarth, b.planetMassEarth)
        ),
        CompareMetric(
            stringResource(R.string.compare_metric_density),
            a.bulkDensity().or { "%.1f g/cm³".format(it) }, b.bulkDensity().or { "%.1f g/cm³".format(it) }
        ),
        CompareMetric(
            stringResource(R.string.compare_metric_gravity),
            a.surfaceGravity().or { "%.2f g".format(it) }, b.surfaceGravity().or { "%.2f g".format(it) },
            ratioOf(a.surfaceGravity(), b.surfaceGravity())
        ),
        CompareMetric(stringResource(R.string.compare_metric_eq_temp), temp(a), temp(b)),
        CompareMetric(
            stringResource(R.string.compare_metric_orbital_period),
            a.orbitalPeriodDays.or(::formatOrbitalPeriod), b.orbitalPeriodDays.or(::formatOrbitalPeriod),
            ratioOf(a.orbitalPeriodDays, b.orbitalPeriodDays)
        ),
        CompareMetric(
            stringResource(R.string.compare_metric_insolation),
            a.insolationFlux.or { "%.2f S⊕".format(it) }, b.insolationFlux.or { "%.2f S⊕".format(it) },
            ratioOf(a.insolationFlux, b.insolationFlux)
        ),
        CompareMetric(stringResource(R.string.compare_metric_star), star(a), star(b)),
        CompareMetric(
            stringResource(R.string.compare_metric_distance),
            a.distanceParsec?.let(::formatLightYears) ?: none, b.distanceParsec?.let(::formatLightYears) ?: none,
            ratioOf(a.distanceParsec, b.distanceParsec)
        ),
        CompareMetric(
            stringResource(R.string.compare_metric_discovery_year),
            a.discoveryYear.toString(), b.discoveryYear.toString()
        ),
    )
}

// ---------------------------------------------------------------------------
// AI scores — mirrored bars growing out from the centre
// ---------------------------------------------------------------------------

@Composable
private fun ScoreRow(label: String, scoreA: Double?, scoreB: Double?) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = AlmanacMeta,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ScorePercent(scoreA, TextAlign.Start)
            ScoreBar(scoreA, growsLeft = true, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(6.dp))
            ScoreBar(scoreB, growsLeft = false, modifier = Modifier.weight(1f))
            ScorePercent(scoreB, TextAlign.End)
        }
    }
}

@Composable
private fun ScorePercent(score: Double?, align: TextAlign) {
    Text(
        text = score?.let { "${(it * 100).toInt()}%" } ?: stringResource(R.string.compare_no_data),
        style = AlmanacData.copy(fontSize = 14.sp, color = score?.let(::scoreColor) ?: InkTextFaint),
        textAlign = align,
        modifier = Modifier.width(48.dp)
    )
}

@Composable
private fun ScoreBar(score: Double?, growsLeft: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(SurfaceCardLight),
        contentAlignment = if (growsLeft) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (score != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score.coerceIn(0.0, 1.0).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(scoreColor(score))
            )
        }
    }
}

/** Ordered union of score categories present in either insight. */
private fun categoryLabels(
    insightA: HabitabilityInsight?,
    insightB: HabitabilityInsight?
): List<String> {
    val ordered = LinkedHashSet<String>()
    insightA?.scores?.keys?.let { ordered.addAll(it) }
    insightB?.scores?.keys?.let { ordered.addAll(it) }
    return ordered.toList()
}

private fun scoreColor(score: Double): Color = when {
    score > 0.7 -> HabitableGreen
    score > 0.4 -> CautionYellow
    else -> HostileRed
}

@Preview
@Composable
private fun VerdictBannerPreview() = PreviewSurface {
    VerdictBanner(PreviewData.planet, PreviewData.hotPlanet, PreviewData.insight, PreviewData.insight)
}

@Preview
@Composable
private fun MetricRowsPreview() = PreviewSurface {
    Column(modifier = Modifier.padding(16.dp)) {
        metricsFor(PreviewData.planet, PreviewData.hotPlanet).forEach { MetricRow(it) }
    }
}
