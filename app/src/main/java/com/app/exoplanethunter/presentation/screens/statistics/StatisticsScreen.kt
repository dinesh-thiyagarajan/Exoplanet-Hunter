package com.app.exoplanethunter.presentation.screens.statistics

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.exoplanet.domain.model.LabelCount
import com.app.exoplanethunter.exoplanet.domain.model.Statistics
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

private val barPalette = listOf(CosmicCyan, NebulaPink, AuroraGreen, StarGold, SolarOrange)

private val sizeOrder = listOf("Earth-size", "Super-Earth", "Neptune-like", "Jupiter-like")

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        StarField(starCount = 100)

        val stats = viewModel.statistics
        if (viewModel.isLoading || stats == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CosmicCyan)
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(colors = listOf(CosmicCyan, NebulaPink))
                )
            )
            Text(
                text = "The catalog at a glance",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hero stat tiles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroStat("Planets", stats.totalPlanets, CosmicCyan, Modifier.weight(1f))
                HeroStat("Star Systems", stats.totalSystems, NebulaPink, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatSection(title = "Discoveries by Method") {
                HorizontalBars(stats.methodCounts)
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatSection(title = "Planet Sizes") {
                val ordered = sizeOrder.mapNotNull { name ->
                    stats.sizeDistribution.firstOrNull { it.label == name }
                }
                HorizontalBars(ordered)
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatSection(title = "Discoveries per Year") {
                YearBars(stats.yearCounts.takeLast(20))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HeroStat(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Text(
            text = "%,d".format(value),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun StatSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun HorizontalBars(data: List<LabelCount>) {
    if (data.isEmpty()) {
        Text("No data", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data.forEachIndexed { index, item ->
            val color = barPalette[index % barPalette.size]
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%,d".format(item.count),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceCardLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.count.toFloat() / max).coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearBars(data: List<LabelCount>) {
    if (data.isEmpty()) {
        Text("No data", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        return
    }
    val max = data.maxOf { it.count }.coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        data.forEach { item ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((110f * (item.count.toFloat() / max)).coerceAtLeast(3f).dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(
                            Brush.verticalGradient(listOf(CosmicCyan, NebulaPink))
                        )
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = data.first().label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
        Text(
            text = data.last().label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}
