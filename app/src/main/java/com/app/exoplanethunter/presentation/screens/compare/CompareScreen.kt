package com.app.exoplanethunter.presentation.screens.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.PlanetDisc
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.SkeletonRow
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.TempIndicator
import com.app.exoplanethunter.presentation.components.bulkDensity
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatLightYears
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.components.isBanded
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.starTypeLabel
import com.app.exoplanethunter.presentation.components.surfaceGravity
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.SectionTitle
import com.app.exoplanethunter.presentation.theme.Surface
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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

    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BackTopBar(
                onBack = onBack,
                backDescription = stringResource(R.string.cd_back),
                title = stringResource(R.string.compare_title),
                modifier = Modifier.topBarInsets(),
            ) {
                if (viewModel.planetA != null && viewModel.planetB != null) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clip(Corners.Pill)
                            .background(Surface)
                            .clickable(onClick = viewModel::swap)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.Swap, null, tint = InkText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.compare_swap), fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkText)
                    }
                }
            }

            val a = viewModel.planetA
            val b = viewModel.planetB

            if (viewModel.isLoading || a == null || b == null) {
                Column(Modifier.padding(16.dp)) { if (viewModel.isLoading) repeat(5) { SkeletonRow() } }
                return@Column
            }

            val insightA = viewModel.insightA
            val insightB = viewModel.insightB

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .screenContentInsets()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // The two worlds, drawn to relative size.
                val largest = max(a.planetRadiusEarth ?: 1.0, b.planetRadiusEarth ?: 1.0)
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlanetCard(a, largest, Modifier.weight(1f), onOpen = { onPlanetClick(a.id) }, onChange = { viewModel.startChange(0) })
                    PlanetCard(b, largest, Modifier.weight(1f), onOpen = { onPlanetClick(b.id) }, onChange = { viewModel.startChange(1) })
                }

                AtAGlance(a, b, insightA, insightB)

                // Paired bars, one property at a time.
                Column(Modifier.padding(top = 8.dp)) {
                    numericMetricsFor(a, b).forEach { PairedBars(it, a, b) }
                    textMetricsFor(a, b).forEach { TextMetricRow(it, a, b) }
                }

                // AI habitability, also as paired bars.
                val scored = insightA?.habitabilityReliable == true || insightB?.habitabilityReliable == true
                if (scored || categoryLabels(insightA, insightB).isNotEmpty()) {
                    Text(stringResource(R.string.compare_section_ml), style = SectionTitle, modifier = Modifier.padding(top = 12.dp))
                    if (scored) {
                        ScoreBars(
                            stringResource(R.string.compare_overall_habitability), a, b,
                            insightA?.overallScore?.takeIf { insightA.habitabilityReliable },
                            insightB?.overallScore?.takeIf { insightB.habitabilityReliable },
                        )
                    }
                    categoryLabels(insightA, insightB).forEach { label ->
                        ScoreBars(label, a, b, insightA?.scores?.get(label), insightB?.scores?.get(label))
                    }
                }

                val anyEstimated = (a.equilibriumTempK == null && a.estimatedTempK() != null) ||
                    (b.equilibriumTempK == null && b.estimatedTempK() != null)
                Text(
                    text = listOfNotNull(
                        stringResource(R.string.compare_estimated_footnote).takeIf { anyEstimated },
                        stringResource(R.string.compare_ml_disclaimer),
                    ).joinToString(" "),
                    style = AlmanacCaption,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                )
            }
        }
    }

    if (viewModel.changingSlot != null) ChangePlanetSheet(viewModel)
}

@Composable
private fun PlanetCard(planet: Exoplanet, largestRadius: Double, modifier: Modifier, onOpen: () -> Unit, onChange: () -> Unit) {
    val bucket = TempBucket.of(planet.estimatedTempK())
    // Area-true sizes would make small worlds vanish; scale by radius with a floor.
    val d = (24 + 52 * ((planet.planetRadiusEarth ?: 1.0) / largestRadius)).coerceIn(24.0, 76.0).dp
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(Corners.SmallCard)
            .background(Surface)
            .clickable(onClick = onOpen)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(80.dp), contentAlignment = Alignment.Center) {
            PlanetDisc(color = bucket.color, size = d, banded = planet.isBanded())
        }
        Spacer(Modifier.height(8.dp))
        Text(planet.planetName, style = CardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        TempIndicator(bucket)
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.compare_change),
            fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Matcha,
            modifier = Modifier.clip(Corners.Pill).clickable(onClick = onChange).padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun AtAGlance(a: Exoplanet, b: Exoplanet, insightA: HabitabilityInsight?, insightB: HabitabilityInsight?) {
    val lines = listOfNotNull(
        earthLikenessHeadline(a, b, insightA, insightB),
        sizeVerdict(a, b),
        distanceVerdict(a, b),
        temperatureVerdict(a, b),
    )
    SectionCard(small = true) {
        Text(stringResource(R.string.compare_verdict_title), style = CardTitle)
        Spacer(Modifier.height(8.dp))
        lines.forEach { line ->
            Text(line, fontFamily = BodyFamily, fontSize = 16.sp, lineHeight = 24.sp, color = InkText, modifier = Modifier.padding(vertical = 3.dp))
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
    return stringResource(
        R.string.compare_verdict_closer,
        closerName,
        formatLightYears(min(da, db)).removeSuffix(" ly"),
        formatLightYears(max(da, db)).removeSuffix(" ly"),
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
// Paired bars
// ---------------------------------------------------------------------------

/**
 * One numeric property for both planets. Bars are drawn against the larger of the two on a
 * square-root scale, so a 50× difference still leaves the smaller bar visible.
 */
private data class NumericMetric(
    val label: String,
    val valueA: Double?,
    val valueB: Double?,
    val textA: String,
    val textB: String,
)

@Composable
private fun PairedBars(metric: NumericMetric, a: Exoplanet, b: Exoplanet) {
    val x = metric.valueA
    val y = metric.valueB
    val top = max(x ?: 0.0, y ?: 0.0)
    val ratio = if (x != null && y != null && x > 0 && y > 0) max(x, y) / min(x, y) else null
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(metric.label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim, modifier = Modifier.weight(1f))
            if (ratio != null && ratio >= 1.1) {
                Text(stringResource(R.string.compare_ratio, if (ratio >= 10) "%,.0f".format(ratio) else "%.1f".format(ratio)), style = AlmanacData.copy(color = Matcha, fontSize = 14.sp))
            }
        }
        Spacer(Modifier.height(6.dp))
        BarLine(a.planetName, x?.let { if (top > 0) sqrt(it / top) else 0.0 }, metric.textA, TempBucket.of(a.estimatedTempK()).color)
        BarLine(b.planetName, y?.let { if (top > 0) sqrt(it / top) else 0.0 }, metric.textB, TempBucket.of(b.estimatedTempK()).color)
    }
}

@Composable
private fun BarLine(name: String, fraction: Double?, text: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = AlmanacCaption, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(76.dp))
        Box(Modifier.weight(1f).height(10.dp).clip(Corners.Pill).background(Raised)) {
            if (fraction != null) {
                Box(Modifier.fillMaxWidth(fraction.coerceIn(0.03, 1.0).toFloat()).fillMaxHeight().clip(Corners.Pill).background(color))
            }
        }
        Text(text, style = AlmanacData.copy(fontSize = 14.sp), textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.width(88.dp))
    }
}

@Composable
private fun ScoreBars(label: String, a: Exoplanet, b: Exoplanet, scoreA: Double?, scoreB: Double?) {
    val none = stringResource(R.string.compare_no_data)
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim)
        Spacer(Modifier.height(6.dp))
        BarLine(a.planetName, scoreA, scoreA?.let { "${(it * 100).toInt()}" } ?: none, Matcha)
        BarLine(b.planetName, scoreB, scoreB?.let { "${(it * 100).toInt()}" } ?: none, Matcha)
    }
}

/** A property shown as words, e.g. the host star: both values side by side. */
private data class TextMetric(val label: String, val textA: String, val textB: String)

@Composable
private fun TextMetricRow(metric: TextMetric, a: Exoplanet, b: Exoplanet) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(metric.label, fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim)
        Spacer(Modifier.height(4.dp))
        Row {
            Column(Modifier.weight(1f)) {
                Text(a.planetName, style = AlmanacCaption, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(metric.textA, style = AlmanacData.copy(fontSize = 14.sp))
            }
            Column(Modifier.weight(1f)) {
                Text(b.planetName, style = AlmanacCaption, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(metric.textB, style = AlmanacData.copy(fontSize = 14.sp))
            }
        }
    }
}

@Composable
private fun numericMetricsFor(a: Exoplanet, b: Exoplanet): List<NumericMetric> {
    val none = stringResource(R.string.compare_no_data)
    fun Double?.or(format: (Double) -> String) = this?.let(format) ?: none
    fun temp(p: Exoplanet): String {
        val t = p.estimatedTempK() ?: return none
        val k = "%,d K".format(t.toInt())
        return if (p.equilibriumTempK == null) "≈ $k" else k
    }
    return listOf(
        NumericMetric(stringResource(R.string.compare_metric_radius), a.planetRadiusEarth, b.planetRadiusEarth,
            a.planetRadiusEarth.or { "%.1f R⊕".format(it) }, b.planetRadiusEarth.or { "%.1f R⊕".format(it) }),
        NumericMetric(stringResource(R.string.compare_metric_mass), a.planetMassEarth, b.planetMassEarth,
            a.planetMassEarth.or { "%,.1f M⊕".format(it) }, b.planetMassEarth.or { "%,.1f M⊕".format(it) }),
        NumericMetric(stringResource(R.string.compare_metric_density), a.bulkDensity(), b.bulkDensity(),
            a.bulkDensity().or { "%.1f g/cm³".format(it) }, b.bulkDensity().or { "%.1f g/cm³".format(it) }),
        NumericMetric(stringResource(R.string.compare_metric_gravity), a.surfaceGravity(), b.surfaceGravity(),
            a.surfaceGravity().or { "%.1f g".format(it) }, b.surfaceGravity().or { "%.1f g".format(it) }),
        NumericMetric(stringResource(R.string.compare_metric_eq_temp), a.estimatedTempK(), b.estimatedTempK(), temp(a), temp(b)),
        NumericMetric(stringResource(R.string.compare_metric_orbital_period), a.orbitalPeriodDays, b.orbitalPeriodDays,
            a.orbitalPeriodDays.or(::formatOrbitalPeriod), b.orbitalPeriodDays.or(::formatOrbitalPeriod)),
        NumericMetric(stringResource(R.string.compare_metric_insolation), a.insolationFlux, b.insolationFlux,
            a.insolationFlux.or { "%,.1f S⊕".format(it) }, b.insolationFlux.or { "%,.1f S⊕".format(it) }),
        NumericMetric(stringResource(R.string.compare_metric_distance), a.distanceParsec, b.distanceParsec,
            formatLightYears(a.distanceParsec), formatLightYears(b.distanceParsec)),
    ).filter { it.valueA != null || it.valueB != null }
}

@Composable
private fun textMetricsFor(a: Exoplanet, b: Exoplanet): List<TextMetric> {
    val none = stringResource(R.string.compare_no_data)
    fun star(p: Exoplanet): String {
        val type = starTypeLabel(p.spectralType)
        val temp = p.stellarEffectiveTempK?.let { "%,d K".format(it.toInt()) }
        return listOfNotNull(type, temp).joinToString(" · ").ifEmpty { none }
    }
    return listOf(
        TextMetric(stringResource(R.string.compare_metric_star), star(a), star(b)),
        TextMetric(stringResource(R.string.compare_metric_discovery_year), a.discoveryYear.toString(), b.discoveryYear.toString()),
    )
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

// ---------------------------------------------------------------------------
// "Change": pick another planet for one side
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePlanetSheet(viewModel: CompareViewModel) {
    ModalBottomSheet(
        onDismissRequest = viewModel::cancelChange,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Raised,
        shape = Corners.Dialog,
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.compare_change_title), style = SectionTitle)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(Corners.Pill)
                    .background(Surface)
                    .border(2.dp, Matcha, Corners.Pill)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (viewModel.pickerQuery.isEmpty()) {
                    Text(stringResource(R.string.search_hint), fontFamily = BodyFamily, fontSize = 16.sp, color = InkTextFaint)
                }
                BasicTextField(
                    value = viewModel.pickerQuery,
                    onValueChange = viewModel::onPickerQueryChanged,
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = BodyFamily, fontSize = 16.sp, color = InkText),
                    cursorBrush = SolidColor(Matcha),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyColumn(Modifier.heightIn(max = 420.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(viewModel.pickerResults, key = { it.id }) { planet ->
                    PlanetRowCard(planet = planet, isFavorite = false, onToggleFavorite = {}, onClick = { viewModel.pick(planet) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
private fun AtAGlancePreview() = PreviewSurface {
    AtAGlance(PreviewData.planet, PreviewData.hotPlanet, PreviewData.insight, PreviewData.insight)
}
