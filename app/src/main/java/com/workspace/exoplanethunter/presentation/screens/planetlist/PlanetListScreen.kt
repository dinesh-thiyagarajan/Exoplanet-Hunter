package com.workspace.exoplanethunter.presentation.screens.planetlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.presentation.components.PlanetMiniRenderer
import com.workspace.exoplanethunter.presentation.components.StarField
import com.workspace.exoplanethunter.presentation.theme.AuroraGreen
import com.workspace.exoplanethunter.presentation.theme.CosmicCyan
import com.workspace.exoplanethunter.presentation.theme.NebulaPink
import com.workspace.exoplanethunter.presentation.theme.SpaceBlack
import com.workspace.exoplanethunter.presentation.theme.StarGold
import com.workspace.exoplanethunter.presentation.theme.SurfaceCard
import com.workspace.exoplanethunter.presentation.theme.SurfaceCardLight
import com.workspace.exoplanethunter.presentation.theme.TextMuted
import com.workspace.exoplanethunter.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetListScreen(
    onPlanetClick: (Long) -> Unit,
    onNavigateToStarSystems: () -> Unit = {},
    viewModel: PlanetListViewModel = viewModel(factory = PlanetListViewModel.Factory)
) {
    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        StarField(starCount = 100)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SpaceBlack,
                                SpaceBlack.copy(alpha = 0.95f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Exoplanet Hunter",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(CosmicCyan, NebulaPink)
                        )
                    )
                )

                Text(
                    text = "${viewModel.planets.size} planets discovered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                TextField(
                    value = viewModel.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text("Search planets or stars...", color = TextMuted)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        cursorColor = CosmicCyan,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.showHabitableOnly,
                            onClick = viewModel::onToggleHabitable,
                            label = { Text("Habitable", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (viewModel.showHabitableOnly) SpaceBlack else AuroraGreen
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = AuroraGreen,
                                selectedLabelColor = SpaceBlack
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = viewModel.selectedFilter == null && !viewModel.showHabitableOnly,
                            onClick = { viewModel.onFilterSelected(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = CosmicCyan,
                                selectedLabelColor = SpaceBlack
                            )
                        )
                    }

                    items(viewModel.discoveryMethods) { method ->
                        FilterChip(
                            selected = viewModel.selectedFilter == method,
                            onClick = { viewModel.onFilterSelected(method) },
                            label = {
                                Text(
                                    text = method.take(20),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = CosmicCyan,
                                selectedLabelColor = SpaceBlack
                            )
                        )
                    }
                }
            }

            // Planet list
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CosmicCyan)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = viewModel.planets,
                        key = { _, planet -> planet.id }
                    ) { index, planet ->
                        AnimatedPlanetCard(
                            planet = planet,
                            index = index,
                            onClick = { onPlanetClick(planet.id) }
                        )
                    }
                }
            }
        }

        // FAB to navigate to Star Systems
        FloatingActionButton(
            onClick = onNavigateToStarSystems,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 32.dp),
            containerColor = StarGold,
            contentColor = SpaceBlack
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Explore Star Systems"
            )
        }
    }
}

@Composable
private fun AnimatedPlanetCard(
    planet: Exoplanet,
    index: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(20) * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 2 }
        )
    ) {
        PlanetCard(planet = planet, onClick = onClick)
    }
}

@Composable
private fun PlanetCard(
    planet: Exoplanet,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlanetMiniRenderer(
                planet = planet,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planet.planetName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = planet.hostName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicCyan,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    planet.planetRadiusEarth?.let {
                        InfoChip("${String.format("%.1f", it)}R\u2295")
                    }
                    planet.equilibriumTempK?.let {
                        InfoChip("${it.toInt()}K")
                    }
                    InfoChip(planet.discoveryYear.toString())
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = planet.discoveryMethod
                        .replace("Radial Velocity", "RV")
                        .replace("Transit Timing Variations", "TTV")
                        .replace("Microlensing", "Lens")
                        .replace("Direct Imaging", "DI")
                        .take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = NebulaPink
                )

                planet.distanceParsec?.let { dist ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.0f", dist)} pc",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceCardLight)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}
