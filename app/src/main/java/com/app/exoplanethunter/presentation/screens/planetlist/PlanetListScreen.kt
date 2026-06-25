package com.app.exoplanethunter.presentation.screens.planetlist

import android.app.Activity
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.InterstitialAdController
import com.app.exoplanethunter.config.FeatureFlags
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.components.AlmanacOutlinedButton
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Surface as SurfaceColor
import com.app.exoplanethunter.presentation.theme.SurfaceRaised
import java.util.Calendar
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

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
    val compareEnabled by FeatureFlags.compareEnabled.collectAsState()

    LaunchedEffect(compareEnabled) {
        if (!compareEnabled && viewModel.compareMode) viewModel.exitCompareMode()
    }

    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ---- Header ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.planet_list_eyebrow), style = AlmanacEyebrow)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.planet_list_title),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%,d".format(viewModel.planets.size),
                            style = AlmanacData.copy(fontSize = 22.sp)
                        )
                        Text(stringResource(R.string.planet_list_confirmed), style = AlmanacMeta)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Underline-only search (no box)
                TextField(
                    value = viewModel.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(stringResource(R.string.planet_list_search_hint), color = InkTextFaint)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = InkTextFaint)
                    },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear), tint = InkTextFaint)
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Brass,
                        focusedIndicatorColor = Brass,
                        unfocusedIndicatorColor = Hairline,
                        focusedTextColor = InkText,
                        unfocusedTextColor = InkText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ---- Filter chips ----
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        val allSelected = viewModel.selectedFilter == null && !viewModel.showHabitableOnly &&
                            !viewModel.showLatestOnly && viewModel.minDiscoveryYear == null
                        AlmanacChip(stringResource(R.string.filter_all), allSelected) { viewModel.onFilterSelected(null) }
                    }
                    item {
                        AlmanacChip(stringResource(R.string.filter_habitable), viewModel.showHabitableOnly) {
                            viewModel.onToggleHabitable()
                        }
                    }
                    items(viewModel.discoveryMethods) { method ->
                        AlmanacChip(method.take(20), viewModel.selectedFilter == method) {
                            viewModel.onFilterSelected(method)
                        }
                    }
                    item {
                        val recentYear = Calendar.getInstance().get(Calendar.YEAR) - 3
                        val isRecent = viewModel.minDiscoveryYear == recentYear
                        AlmanacChip(stringResource(R.string.filter_recent_3y), isRecent) {
                            viewModel.onMinYearChanged(if (isRecent) null else recentYear)
                        }
                    }
                    item {
                        AlmanacChip(stringResource(R.string.filter_latest), viewModel.showLatestOnly) {
                            viewModel.onToggleLatest()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---- Sort + compare row ----
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            R.string.planet_list_sorted_by,
                            stringResource(viewModel.sortOption.labelRes).uppercase()
                        ),
                        style = AlmanacSectionLabel,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSortSheet = true }
                    )
                    if (compareEnabled) {
                        AlmanacOutlinedButton(
                            label = stringResource(R.string.compare_action).uppercase(),
                            onClick = viewModel::toggleCompareMode,
                            active = viewModel.compareMode
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Hairline))
            }

            // ---- List ----
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brass)
                }
            } else if (viewModel.planets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.planet_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkTextFaint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 10.dp,
                        bottom = if (viewModel.compareMode) 96.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val planets = viewModel.planets
                    planets.forEachIndexed { index, planet ->
                        item(key = planet.id) {
                            AnimatedPlanetCard(
                                planet = planet,
                                index = index,
                                isYearHighlighted = viewModel.showLatestOnly || viewModel.minDiscoveryYear != null,
                                isFavorite = planet.planetName in viewModel.favoriteNames,
                                isSelectedForCompare = viewModel.compareMode && viewModel.isSelectedForCompare(planet),
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
                        if ((index + 1) % 5 == 0 && index < planets.size - 1) {
                            item(key = "ad_planet_$index") { AdBannerCard() }
                        }
                    }
                }
            }
        }

        // ---- Compare action bar ----
        if (viewModel.compareMode) {
            val selected = viewModel.selectedForCompare
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                color = SurfaceColor,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selected.size < 2) stringResource(R.string.compare_hint)
                            else selected.joinToString(" vs ") { it.planetName },
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.compare_selected_count, selected.size),
                            style = AlmanacMeta
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (selected.size == 2) {
                                val a = selected[0]; val b = selected[1]
                                viewModel.trackComparison(a, b)
                                InterstitialAdController.maybeShow(activity) { onCompare(a.id, b.id) }
                                viewModel.exitCompareMode()
                            }
                        },
                        enabled = selected.size == 2,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brass,
                            contentColor = Ink,
                            disabledContainerColor = SurfaceRaised,
                            disabledContentColor = InkTextFaint
                        )
                    ) {
                        Text(stringResource(R.string.compare_button), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // ---- Sort sheet ----
        if (showSortSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState,
                containerColor = SurfaceColor
            ) {
                Text(
                    text = stringResource(R.string.sort_title),
                    style = MaterialTheme.typography.titleLarge,
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
                            color = if (selected) Brass else InkText,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Brass)
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
        modifier = Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 24f
        }
    ) {
        PlanetRowCard(
            planet = planet,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onClick = onClick,
            isYearHighlighted = isYearHighlighted,
            isSelected = isSelectedForCompare
        )
    }
}
