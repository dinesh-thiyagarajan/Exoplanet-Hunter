package com.app.exoplanethunter.presentation.screens.compare
import androidx.compose.ui.tooling.preview.Preview
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.preview.PreviewData

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.app.exoplanethunter.presentation.components.PlanetMiniRenderer
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CautionYellow
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.HabitableGreen
import com.app.exoplanethunter.presentation.theme.HostileRed
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

/** A single comparable metric: a label, the two values, and how to read "bigger". */
private data class CompareMetric(
    val label: String,
    val valueA: Double?,
    val valueB: Double?,
    val displayA: String,
    val displayB: String
)

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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Planet headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    PlanetHeader(planet = a, modifier = Modifier.weight(1f), onClick = { onPlanetClick(a.id) })
                    PlanetHeader(planet = b, modifier = Modifier.weight(1f), onClick = { onPlanetClick(b.id) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plain-language summary of the most meaningful differences
                VerdictBanner(a, b, insightA, insightB)

                Spacer(modifier = Modifier.height(20.dp))

                // Radius-proportional visual, with Earth for reference
                SizeComparison(a, b)

                Spacer(modifier = Modifier.height(20.dp))

                // Classification badges
                Row(modifier = Modifier.fillMaxWidth()) {
                    ClassificationCell(insightA?.classification, Modifier.weight(1f))
                    ClassificationCell(insightB?.classification, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── ML Habitability Analysis ──
                SectionHeader(stringResource(R.string.compare_section_ml))

                OverallScoreRow(insightA, insightB)

                Spacer(modifier = Modifier.height(8.dp))

                categoryLabels(insightA, insightB).forEach { label ->
                    CategoryRow(
                        label = label,
                        scoreA = insightA?.scores?.get(label),
                        scoreB = insightB?.scores?.get(label)
                    )
                }

                Text(
                    text = stringResource(R.string.compare_ml_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Physical properties ──
                SectionHeader(stringResource(R.string.compare_section_properties))

                metricsFor(a, b).forEach { metric ->
                    MetricRow(metric)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

private val EarthBlue = Color(0xFF2E6FB7)
private val EarthBlueLight = Color(0xFF7FB3E8)

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicCyan.copy(alpha = 0.08f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.compare_verdict_title),
                style = MaterialTheme.typography.labelMedium,
                color = CosmicCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            supporting.forEach { line ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "•  $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
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

    // Prefer the ML habitability score when both are reliable, else fall back to a
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
    val ta = a.equilibriumTempK
    val tb = b.equilibriumTempK
    if (ta == null || tb == null || abs(ta - tb) < 1.0) return null
    val hotter = if (ta >= tb) a.planetName else b.planetName
    return stringResource(R.string.compare_verdict_hotter, hotter)
}

/** Radius/temperature penalty vs Earth (lower = closer to Earth); null when unknown. */
private fun earthPenalty(planet: Exoplanet): Double? {
    val r = planet.planetRadiusEarth
    val t = planet.equilibriumTempK
    if (r == null && t == null) return null
    var penalty = 0.0
    if (r != null && r > 0.0) penalty += abs(log10(r))
    if (t != null) penalty += abs(t - 255.0) / 255.0
    return penalty
}

@Composable
private fun SizeComparison(a: Exoplanet, b: Exoplanet) {
    val maxRadius = listOfNotNull(a.planetRadiusEarth, b.planetRadiusEarth, 1.0).max()

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.compare_size_title))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            PlanetSizeBody(name = a.planetName, planet = a, radius = a.planetRadiusEarth, maxRadius = maxRadius)
            EarthSizeBody(maxRadius = maxRadius)
            PlanetSizeBody(name = b.planetName, planet = b, radius = b.planetRadiusEarth, maxRadius = maxRadius)
        }
    }
}

@Composable
private fun PlanetSizeBody(name: String, planet: Exoplanet, radius: Double?, maxRadius: Double) {
    Column(
        modifier = Modifier
            .widthIn(max = 110.dp)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlanetMiniRenderer(planet = planet, size = sizeForRadius(radius, maxRadius))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (radius != null) stringResource(R.string.compare_radius_value, String.format("%.2f", radius))
            else stringResource(R.string.compare_no_data),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun EarthSizeBody(maxRadius: Double) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(sizeForRadius(1.0, maxRadius))
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(EarthBlueLight, EarthBlue)))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.compare_earth_label),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.compare_radius_value, "1.00"),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

/** Radius mapped linearly to a circle diameter, clamped so tiny worlds stay visible. */
private fun sizeForRadius(radius: Double?, maxRadius: Double): Dp {
    val maxSize = 104.dp
    val minSize = 22.dp
    if (radius == null || radius <= 0.0 || maxRadius <= 0.0) return minSize
    val fraction = (radius / maxRadius).coerceIn(0.0, 1.0)
    return (maxSize.value * fraction).dp.coerceAtLeast(minSize)
}

@Composable
private fun ClassificationCell(classification: PlanetClassification?, modifier: Modifier = Modifier) {
    val resolved = classification ?: PlanetClassification.UNKNOWN
    val color = classificationColor(resolved)
    Box(modifier = modifier.padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = resolved.label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OverallScoreRow(insightA: HabitabilityInsight?, insightB: HabitabilityInsight?) {
    val aReliable = insightA?.habitabilityReliable == true
    val bReliable = insightB?.habitabilityReliable == true
    val aWins = aReliable && bReliable && insightA!!.overallScore > insightB!!.overallScore
    val bWins = aReliable && bReliable && insightB!!.overallScore > insightA!!.overallScore

    Column {
        Text(
            text = stringResource(R.string.compare_overall_habitability),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OverallScoreCell(insightA, highlighted = aWins, modifier = Modifier.weight(1f))
            OverallScoreCell(insightB, highlighted = bWins, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverallScoreCell(
    insight: HabitabilityInsight?,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val reliable = insight?.habitabilityReliable == true
    val text = if (reliable) "${(insight!!.overallScore * 100).toInt()}%"
    else stringResource(R.string.compare_no_data)
    val color = if (reliable) scoreColor(insight!!.overallScore) else TextMuted

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) color.copy(alpha = 0.15f) else SurfaceCard)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun CategoryRow(label: String, scoreA: Double?, scoreB: Double?) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            CategoryCell(scoreA, modifier = Modifier.weight(1f))
            CategoryCell(scoreB, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryCell(score: Double?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (score != null) "${(score * 100).toInt()}%"
            else stringResource(R.string.compare_no_data),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (score != null) scoreColor(score) else TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceCardLight)
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

private fun classificationColor(classification: PlanetClassification): Color = when (classification) {
    PlanetClassification.POTENTIALLY_HABITABLE -> HabitableGreen
    PlanetClassification.ROCKY -> CosmicCyan
    PlanetClassification.SUPER_EARTH -> AuroraGreen
    PlanetClassification.SUB_EARTH -> CautionYellow
    PlanetClassification.NEPTUNE_LIKE -> NebulaPink
    PlanetClassification.GAS_GIANT -> SolarOrange
    PlanetClassification.UNKNOWN -> TextMuted
}

private fun scoreColor(score: Double): Color = when {
    score > 0.7 -> HabitableGreen
    score > 0.4 -> CautionYellow
    else -> HostileRed
}

@Composable
private fun PlanetHeader(planet: Exoplanet, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlanetMiniRenderer(planet = planet, size = 72.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = planet.planetName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = planet.hostName,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetricRow(metric: CompareMetric) {
    // Highlight the larger value in cyan; leave both neutral when equal or unknown.
    val aWins = metric.valueA != null && metric.valueB != null && metric.valueA > metric.valueB
    val bWins = metric.valueA != null && metric.valueB != null && metric.valueB > metric.valueA

    // Interpretive "how different" factor, shown next to the label.
    val ratioText: String? = run {
        val x = metric.valueA
        val y = metric.valueB
        if (x != null && y != null && x > 0.0 && y > 0.0) {
            val factor = max(x, y) / min(x, y)
            if (factor >= 1.1) String.format("%.1f", factor) else null
        } else null
    }

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            if (ratioText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.compare_ratio, ratioText),
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricValue(text = metric.displayA, highlighted = aWins, modifier = Modifier.weight(1f))
            MetricValue(text = metric.displayB, highlighted = bWins, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricValue(text: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) CosmicCyan.copy(alpha = 0.15f) else SurfaceCard)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (highlighted) CosmicCyan else TextSecondary
        )
    }
}

@Composable
private fun metricsFor(a: Exoplanet, b: Exoplanet): List<CompareMetric> = listOf(
    metric(
        stringResource(R.string.compare_metric_radius),
        a.planetRadiusEarth, b.planetRadiusEarth, decimals = 2
    ),
    metric(
        stringResource(R.string.compare_metric_mass),
        a.planetMassEarth, b.planetMassEarth, decimals = 2
    ),
    metric(
        stringResource(R.string.compare_metric_orbital_period),
        a.orbitalPeriodDays, b.orbitalPeriodDays, decimals = 1
    ),
    metric(
        stringResource(R.string.compare_metric_eq_temp),
        a.equilibriumTempK, b.equilibriumTempK, decimals = 0
    ),
    metric(
        stringResource(R.string.compare_metric_insolation),
        a.insolationFlux, b.insolationFlux, decimals = 2
    ),
    metric(
        stringResource(R.string.compare_metric_distance),
        a.distanceParsec, b.distanceParsec, decimals = 2
    ),
    metric(
        stringResource(R.string.compare_metric_discovery_year),
        a.discoveryYear.toDouble(), b.discoveryYear.toDouble(), decimals = 0,
        groupThousands = false
    )
)

@Composable
private fun metric(
    label: String,
    valueA: Double?,
    valueB: Double?,
    decimals: Int,
    groupThousands: Boolean = true
): CompareMetric {
    val none = stringResource(R.string.compare_no_data)
    return CompareMetric(
        label = label,
        valueA = valueA,
        valueB = valueB,
        displayA = format(valueA, decimals, groupThousands, none),
        displayB = format(valueB, decimals, groupThousands, none)
    )
}

private fun format(value: Double?, decimals: Int, groupThousands: Boolean, none: String): String {
    if (value == null) return none
    val pattern = if (groupThousands) "%,.${decimals}f" else "%.${decimals}f"
    return String.format(pattern, value)
}

@Preview
@Composable
private fun VerdictBannerPreview() = PreviewSurface {
    VerdictBanner(PreviewData.planet, PreviewData.hotPlanet, PreviewData.insight, PreviewData.insight)
}

@Preview
@Composable
private fun SizeComparisonPreview() = PreviewSurface {
    SizeComparison(PreviewData.planet, PreviewData.hotPlanet)
}
