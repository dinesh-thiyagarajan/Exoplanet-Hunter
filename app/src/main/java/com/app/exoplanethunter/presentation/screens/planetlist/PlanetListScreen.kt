package com.app.exoplanethunter.presentation.screens.planetlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import java.util.Calendar
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.InterstitialAdController
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetListScreen(
    onPlanetClick: (Long) -> Unit,
    onCompare: (Long, Long) -> Unit = { _, _ -> },
    viewModel: PlanetListViewModel = koinViewModel()
) {
    val listState = rememberLazyListState()
    var showSortSheet by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

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
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exoplanets",
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
                    }

                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort_action),
                            tint = if (viewModel.sortOption == SortOption.DEFAULT) TextSecondary else CosmicCyan
                        )
                    }
                    IconButton(onClick = viewModel::toggleCompareMode) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = stringResource(R.string.compare_action),
                            tint = if (viewModel.compareMode) CosmicCyan else TextSecondary
                        )
                    }
                }

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
                            selected = viewModel.selectedFilter == null && !viewModel.showHabitableOnly && !viewModel.showLatestOnly && viewModel.minDiscoveryYear == null,
                            onClick = { viewModel.onFilterSelected(null) },
                            label = { Text(stringResource(R.string.filter_all), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = CosmicCyan,
                                selectedLabelColor = SpaceBlack
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = viewModel.showHabitableOnly,
                            onClick = viewModel::onToggleHabitable,
                            label = { Text(stringResource(R.string.filter_habitable), fontSize = 12.sp) },
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

                    item {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val recentYear = currentYear - 3
                        val isRecentSelected = viewModel.minDiscoveryYear == recentYear
                        
                        FilterChip(
                            selected = isRecentSelected,
                            onClick = { 
                                if (isRecentSelected) viewModel.onMinYearChanged(null) 
                                else viewModel.onMinYearChanged(recentYear) 
                            },
                            label = { Text(stringResource(R.string.filter_recent_3y), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = CosmicCyan,
                                selectedLabelColor = SpaceBlack
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = viewModel.showLatestOnly,
                            onClick = viewModel::onToggleLatest,
                            label = { Text(stringResource(R.string.filter_latest), fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (viewModel.showLatestOnly) SpaceBlack else StarGold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = StarGold,
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
                if (viewModel.planets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No planets found matching your criteria.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = if (viewModel.compareMode) 96.dp else 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val planets = viewModel.planets
                        planets.forEachIndexed { index, planet ->
                            item(key = planet.id) {
                                AnimatedPlanetCard(
                                    planet = planet,
                                    index = index,
                                    isYearHighlighted = viewModel.showLatestOnly || viewModel.minDiscoveryYear != null,
                                    isFavorite = planet.planetName in viewModel.favoriteNames,
                                    isSelectedForCompare = viewModel.compareMode &&
                                        viewModel.isSelectedForCompare(planet),
                                    onToggleFavorite = { viewModel.toggleFavorite(planet) },
                                    onClick = {
                                        if (viewModel.compareMode) {
                                            viewModel.onCompareSelect(planet)
                                        } else {
                                            viewModel.trackPlanetClicked(planet)
                                            onPlanetClick(planet.id)
                                        }
                                    }
                                )
                            }
                            // Interleave an ad after every 5th planet (not at the end of the list).
                            if ((index + 1) % 5 == 0 && index < planets.size - 1) {
                                item(key = "ad_planet_$index") {
                                    AdBannerCard()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Compare action bar (only while in compare mode)
        if (viewModel.compareMode) {
            val selected = viewModel.selectedForCompare
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = SurfaceCard,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selected.size < 2) stringResource(R.string.compare_hint)
                            else selected.joinToString(" vs ") { it.planetName },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.compare_selected_count, selected.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (selected.size == 2) {
                                val a = selected[0]
                                val b = selected[1]
                                viewModel.trackComparison(a, b)
                                InterstitialAdController.maybeShow(activity) {
                                    onCompare(a.id, b.id)
                                }
                                viewModel.exitCompareMode()
                            }
                        },
                        enabled = selected.size == 2,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicCyan,
                            contentColor = SpaceBlack,
                            disabledContainerColor = SurfaceCardLight,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.compare_button),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sort bottom sheet
        if (showSortSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState,
                containerColor = SurfaceCard
            ) {
                Text(
                    text = stringResource(R.string.sort_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )
                SortOption.entries.forEach { option ->
                    val selected = viewModel.sortOption == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onSortSelected(option)
                                showSortSheet = false
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(option.labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) CosmicCyan else Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CosmicCyan)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AnimatedPlanetCard(
    planet: Exoplanet,
    index: Int,
    isYearHighlighted: Boolean,
    isFavorite: Boolean,
    isSelectedForCompare: Boolean = false,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(10) * 30L)
        progress.animateTo(1f, animationSpec = tween(250))
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 24f
            }
            .then(
                if (isSelectedForCompare) {
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, CosmicCyan, RoundedCornerShape(20.dp))
                } else Modifier
            )
    ) {
        PlanetRowCard(
            planet = planet,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onClick = onClick,
            isYearHighlighted = isYearHighlighted
        )
    }
}

