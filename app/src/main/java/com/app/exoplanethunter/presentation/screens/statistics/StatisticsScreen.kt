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
import org.koin.androidx.compose.koinViewModel

private val sizeOrder = listOf("Earth-size", "Super-Earth", "Neptune-like", "Jupiter-like")

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        val stats = viewModel.statistics
        if (viewModel.isLoading || stats == null) {
            CircularProgressIndicator(color = Brass, modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.stats_eyebrow), style = AlmanacEyebrow)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.stats_glance), style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(20.dp))

            // Hero figure
            Text("%,d".format(stats.totalPlanets), style = AlmanacHeroFigure)
            Text(stringResource(R.string.stats_confirmed_worlds), style = AlmanacSectionLabel)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("%,d".format(stats.totalSystems), style = AlmanacData)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.stats_star_systems), style = AlmanacMeta)
            }

            Spacer(modifier = Modifier.height(28.dp))

            StatSection(stringResource(R.string.stats_how_found)) {
                MethodBars(stats.methodCounts)
            }

            Spacer(modifier = Modifier.height(20.dp))
            AdBannerCard()
            Spacer(modifier = Modifier.height(20.dp))

            StatSection(stringResource(R.string.stats_section_years).uppercase()) {
                YearArea(stats.yearCounts)
            }

            Spacer(modifier = Modifier.height(20.dp))

            StatSection(stringResource(R.string.stats_section_transit).uppercase()) {
                TransitMethodContent()
            }

            Spacer(modifier = Modifier.height(20.dp))

            StatSection(stringResource(R.string.stats_section_radial_velocity).uppercase()) {
                RadialVelocityContent()
            }

            Spacer(modifier = Modifier.height(20.dp))

            StatSection(stringResource(R.string.stats_section_sizes).uppercase()) {
                val ordered = sizeOrder.mapNotNull { name -> stats.sizeDistribution.firstOrNull { it.label == name } }
                MethodBars(ordered, leaderOnly = false)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = AlmanacSectionLabel)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Hairline))
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

/**
 * Horizontal bars. By default only the leading bar is brass (the single accent)
 * and the rest are neutral, as in the design; pass [leaderOnly] = false to keep
 * that behaviour for any ordered series.
 */
@Composable
private fun MethodBars(data: List<LabelCount>, leaderOnly: Boolean = true) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.stats_no_data), style = AlmanacMeta)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        data.forEachIndexed { index, item ->
            val isLeader = index == 0
            val barColor = if (leaderOnly && isLeader) Brass else if (!leaderOnly) Brass else InkTextFaint
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%,d".format(item.count), style = AlmanacData)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SurfaceRaised)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.count.toFloat() / max).coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

/** A filled brass area chart of discoveries per year. */
@Composable
private fun YearArea(data: List<LabelCount>) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.stats_no_data), style = AlmanacMeta)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val n = data.size
            fun px(i: Int) = if (n <= 1) 0f else i.toFloat() / (n - 1) * w
            fun py(c: Int) = h - (c.toFloat() / max) * (h * 0.9f)

            val line = Path().apply {
                moveTo(0f, py(data[0].count))
                data.forEachIndexed { i, d -> lineTo(px(i), py(d.count)) }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(
                area,
                brush = Brush.verticalGradient(listOf(Brass.copy(alpha = 0.35f), Brass.copy(alpha = 0.02f)))
            )
            drawPath(line, color = Brass, style = Stroke(width = 2f, cap = StrokeCap.Round))
            drawLine(Hairline, Offset(0f, h), Offset(w, h), strokeWidth = 1f)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.first().label, style = AlmanacMeta)
            Text(data.last().label, style = AlmanacMeta)
        }
    }
}
