package com.app.exoplanethunter.presentation.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.exoplanet.domain.model.LabelCount
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.AlmanacHeroFigure
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Surface
import com.app.exoplanethunter.presentation.theme.SurfaceRaised
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.SkeletonRow
import com.app.exoplanethunter.presentation.components.TabHeader
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.MatchaMid
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.SectionTitle
import com.app.exoplanethunter.presentation.theme.StarOrange
import org.koin.androidx.compose.koinViewModel

private val sizeOrder = listOf("Earth-size", "Super-Earth", "Neptune-like", "Jupiter-like")

@Composable
fun StatisticsScreen(
    onMethodSelected: (String) -> Unit = {},
    viewModel: StatisticsViewModel = koinViewModel()
) {
    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        TabHeader(title = stringResource(R.string.stats_title), subtitle = null)
        val stats = viewModel.statistics
        if (viewModel.isLoading || stats == null) {
            Column(Modifier.padding(16.dp)) { repeat(6) { SkeletonRow() } }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat("%,d".format(stats.totalPlanets), stringResource(R.string.stats_confirmed_worlds), Matcha, Modifier.weight(1f))
                HeroStat("%,d".format(stats.totalSystems), stringResource(R.string.stats_star_systems), StarOrange, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            StatSection(stringResource(R.string.stats_how_found), trailing = stringResource(R.string.stats_tap_to_filter)) {
                // The four biggest methods, then everything else as one "Other" bar.
                val sorted = stats.methodCounts.sortedByDescending { it.count }
                val other = stringResource(R.string.stats_other)
                val grouped = if (sorted.size <= 5) sorted else
                    sorted.take(4) + LabelCount(other, sorted.drop(4).sumOf { it.count })
                MethodBars(grouped, onSelect = { if (it != other) onMethodSelected(it) })
            }

            Spacer(modifier = Modifier.height(20.dp))
            AdBannerCard()
            Spacer(modifier = Modifier.height(8.dp))

            StatSection(stringResource(R.string.stats_section_years)) {
                YearColumns(stats.yearCounts)
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionCard {
                Text(stringResource(R.string.stats_section_transit), style = CardTitle)
                Spacer(Modifier.height(12.dp))
                TransitMethodContent()
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionCard {
                Text(stringResource(R.string.stats_section_radial_velocity), style = CardTitle)
                Spacer(Modifier.height(12.dp))
                RadialVelocityContent()
            }

            Spacer(modifier = Modifier.height(28.dp))

            StatSection(stringResource(R.string.stats_section_sizes)) {
                val ordered = sizeOrder.mapNotNull { name -> stats.sizeDistribution.firstOrNull { it.label == name } }
                MethodBars(ordered)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(Corners.SmallCard)
            .background(Surface)
            .padding(20.dp)
    ) {
        Text(value, style = AlmanacHeroFigure.copy(color = color))
        Spacer(Modifier.height(4.dp))
        Text(label, fontFamily = BodyFamily, fontSize = 14.sp, color = InkTextDim)
    }
}

@Composable
private fun StatSection(title: String, trailing: String? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = SectionTitle, modifier = Modifier.weight(1f))
            if (trailing != null) Text(trailing, style = AlmanacCaption)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

/**
 * Horizontal bars with the label on the left and the count on the right. When [onSelect] is
 * given, a tap on a bar filters the planet list to it.
 */
@Composable
private fun MethodBars(data: List<LabelCount>, onSelect: ((String) -> Unit)? = null) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.stats_no_data), style = AlmanacMeta)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Corners.Pill)
                    .then(if (onSelect != null) Modifier.clickable { onSelect(item.label) } else Modifier)
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.label,
                    fontFamily = BodyFamily,
                    fontSize = 15.sp,
                    color = InkTextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(124.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(Corners.Pill)
                        .background(Raised)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.count.toFloat() / max).coerceIn(0.03f, 1f))
                            .fillMaxHeight()
                            .clip(Corners.Pill)
                            .background(Matcha)
                    )
                }
                Text("%,d".format(item.count), style = AlmanacData, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
            }
        }
    }
}

/** Discoveries per year as columns, with the busiest year picked out and labelled. */
@Composable
private fun YearColumns(data: List<LabelCount>) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.stats_no_data), style = AlmanacMeta)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    val peak = data.maxBy { it.count }
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val n = data.size
            val slot = size.width / n
            val bar = (slot * 0.7f).coerceAtLeast(1.5f)
            data.forEachIndexed { i, d ->
                val h = (d.count.toFloat() / max * size.height).coerceAtLeast(2.dp.toPx())
                drawRoundRect(
                    color = if (d == peak) Matcha else MatchaMid.copy(alpha = 0.75f),
                    topLeft = Offset(i * slot + (slot - bar) / 2, size.height - h),
                    size = Size(bar, h),
                    cornerRadius = CornerRadius(bar / 2, bar / 2),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.first().label, style = AlmanacCaption)
            Text(stringResource(R.string.stats_peak_year, peak.label, "%,d".format(peak.count)), style = AlmanacCaption.copy(color = InkTextDim))
            Text(data.last().label, style = AlmanacCaption)
        }
    }
}

