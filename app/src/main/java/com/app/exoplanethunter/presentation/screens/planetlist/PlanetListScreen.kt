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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.bannerAdsVisible
import com.app.exoplanethunter.ads.rememberBannerAdPool
import com.app.exoplanethunter.ads.InterstitialAdController
import com.app.exoplanethunter.config.FeatureFlags
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.components.AlmanacOutlinedButton
import com.app.exoplanethunter.presentation.components.BackToTopButton
import com.app.exoplanethunter.presentation.components.CollapsingHeaderLayout
import com.app.exoplanethunter.presentation.components.rememberCollapsingHeaderState
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.CircleIconButton
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.PrimaryButton
import com.app.exoplanethunter.presentation.components.SkeletonRow
import com.app.exoplanethunter.presentation.components.TabHeader
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.MatchaDeep
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.SectionTitle
import java.util.Calendar
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetListScreen(
    onPlanetClick: (Long) -> Unit,
    onCompare: (Long, Long) -> Unit = { _, _ -> },
    onOpenSearch: () -> Unit = {},
    viewModel: PlanetListViewModel = koinViewModel()
) {
    val listState = rememberLazyListState()
    val animatedPlanetIds = remember { mutableSetOf<Long>() }
    val headerState = rememberCollapsingHeaderState()
    val focusManager = LocalFocusManager.current
    val adPool = rememberBannerAdPool()
    var showSortSheet by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    val compareEnabled by FeatureFlags.compareEnabled.collectAsState()

    LaunchedEffect(compareEnabled) {
        if (!compareEnabled && viewModel.compareMode) viewModel.exitCompareMode()
    }

    // New filter / search / sort results start at the top, not at the old list's position.
    var handledScrollReset by rememberSaveable { mutableIntStateOf(viewModel.scrollResetCount) }
    LaunchedEffect(viewModel.scrollResetCount) {
        if (viewModel.scrollResetCount != handledScrollReset) {
            handledScrollReset = viewModel.scrollResetCount
            listState.scrollToItem(0)
            headerState.expand()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        CollapsingHeaderLayout(
            state = headerState,
            onUserScroll = { focusManager.clearFocus() },
            header = {
                // ---- Header (hides on scroll down, returns on scroll up) ----
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Ink)
                        .padding(bottom = 4.dp)
                ) {
                    val total = viewModel.totalPlanetCount
                    val shown = viewModel.planets.size
                    val isFiltered = total > 0 && shown != total
                    TabHeader(
                        title = stringResource(R.string.planet_list_title),
                        subtitle = when {
                            total == 0 -> null
                            isFiltered -> stringResource(R.string.planet_list_filtered, "%,d".format(shown), "%,d".format(total))
                            else -> stringResource(R.string.planet_list_subtitle, "%,d".format(total))
                        },
                    ) {
                        CircleIconButton(AppIcons.Search, stringResource(R.string.search_action), onOpenSearch)
                        CircleIconButton(AppIcons.Filter, stringResource(R.string.sort_action), { showSortSheet = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---- Filter chips ----
                    // Keep the selected chip in view, e.g. after filtering from the Stats tab.
                    val chipState = rememberLazyListState()
                    LaunchedEffect(viewModel.selectedFilter, viewModel.discoveryMethods) {
                        val i = viewModel.discoveryMethods.indexOf(viewModel.selectedFilter)
                        if (i >= 0) chipState.animateScrollToItem(i + 2)
                    }
                    LazyRow(
                        state = chipState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
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

                    // ---- "Sorted by Default · Compare" ----
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.planet_list_sorted_prefix))
                                append(" ")
                                withStyle(SpanStyle(color = InkText, fontWeight = FontWeight.Bold)) {
                                    append(stringResource(viewModel.sortOption.labelRes))
                                }
                            },
                            style = AlmanacMeta,
                            modifier = Modifier
                                .clip(Corners.Pill)
                                .clickable { showSortSheet = true }
                                .padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (compareEnabled) {
                            AlmanacOutlinedButton(
                                label = stringResource(if (viewModel.compareMode) R.string.compare_exit else R.string.compare_action),
                                onClick = viewModel::toggleCompareMode,
                                active = viewModel.compareMode
                            )
                        }
                    }
                }
            },
        ) { headerHeight ->
            // ---- List ----
            if (viewModel.isLoading) {
                Column(modifier = Modifier.fillMaxSize().padding(top = headerHeight, start = 16.dp, end = 16.dp)) {
                    repeat(8) { SkeletonRow() }
                }
            } else if (viewModel.planets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = headerHeight)) {
                    EmptyState(
                        title = stringResource(R.string.planet_list_empty_title),
                        hint = stringResource(R.string.planet_list_empty),
                        actionLabel = stringResource(R.string.planet_list_clear_filters),
                        onAction = { viewModel.onFilterSelected(null) },
                    )
                }
            } else {
                val showAds = bannerAdsVisible()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 4.dp, top = headerHeight + 4.dp,
                        bottom = if (viewModel.compareMode) 112.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val planets = viewModel.planets
                    planets.forEachIndexed { index, planet ->
                        item(key = planet.id) {
                            val animateIn = remember {
                                index < ANIMATED_ROW_LIMIT && animatedPlanetIds.add(planet.id)
                            }
                            AnimatedPlanetCard(
                                planet = planet,
                                index = index,
                                animateIn = animateIn,
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
                        if (showAds && (index + 1) % 5 == 0 && index < planets.size - 1) {
                            item(key = "ad_planet_$index") { AdBannerCard(pool = adPool, slot = index / 5) }
                        }
                    }
                }
            }
        }

        BackToTopButton(
            listState = listState,
            headerState = headerState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (viewModel.compareMode) 96.dp else 16.dp)
        )

        // ---- Compare action bar ----
        if (viewModel.compareMode) {
            val selected = viewModel.selectedForCompare
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .clip(Corners.Pill)
                    .background(MatchaDeep)
                    .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selected.size < 2) stringResource(R.string.compare_hint)
                        else selected.joinToString(" vs ") { it.planetName },
                        style = MaterialTheme.typography.titleSmall,
                        color = InkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.compare_selected_count, selected.size),
                        style = AlmanacMeta.copy(color = OnMatchaDeep)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                PrimaryButton(
                    label = stringResource(R.string.compare_button),
                    enabled = selected.size == 2,
                    onClick = {
                        if (selected.size == 2) {
                            val a = selected[0]; val b = selected[1]
                            viewModel.trackComparison(a, b)
                            InterstitialAdController.maybeShow(activity) { onCompare(a.id, b.id) }
                            viewModel.exitCompareMode()
                        }
                    }
                )
            }
        }

        // ---- Sort sheet ----
        if (showSortSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState,
                containerColor = Raised,
                shape = Corners.Dialog
            ) {
                Text(
                    text = stringResource(R.string.sort_title),
                    style = SectionTitle,
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
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Matcha else InkText,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) Icon(AppIcons.Check, contentDescription = null, tint = Matcha, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Rows only animate the first time they appear near the top of a list (first load or a new
 * filter/search result) — never when scrolled back into view, which made fast scrolls flicker.
 */
private const val ANIMATED_ROW_LIMIT = 12

@Composable
private fun AnimatedPlanetCard(
    planet: Exoplanet,
    index: Int,
    animateIn: Boolean,
    isYearHighlighted: Boolean,
    isFavorite: Boolean,
    isSelectedForCompare: Boolean = false,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val progress = remember { Animatable(if (animateIn) 0f else 1f) }
    if (animateIn) LaunchedEffect(Unit) {
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
