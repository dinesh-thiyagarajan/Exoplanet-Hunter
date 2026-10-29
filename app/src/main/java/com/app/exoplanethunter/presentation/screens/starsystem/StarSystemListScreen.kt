package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.bannerAdsVisible
import com.app.exoplanethunter.ads.rememberBannerAdPool
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BackToTopButton
import com.app.exoplanethunter.presentation.components.CircleIconButton
import com.app.exoplanethunter.presentation.components.CollapsingHeaderLayout
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.SkeletonRow
import com.app.exoplanethunter.presentation.components.StarSystemRow
import com.app.exoplanethunter.presentation.components.TabHeader
import com.app.exoplanethunter.presentation.components.rememberCollapsingHeaderState
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.OnMatcha
import com.app.exoplanethunter.presentation.theme.StarGlow
import com.app.exoplanethunter.presentation.theme.Surface
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun StarSystemListScreen(
    onSystemClick: (Long) -> Unit,
    onOpenGalaxyMap: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    viewModel: StarSystemListViewModel = koinViewModel(),
) {
    val listState = rememberLazyListState()
    val animatedSystemIds = remember { mutableSetOf<Long>() }
    val headerState = rememberCollapsingHeaderState()
    val focusManager = LocalFocusManager.current
    val adPool = rememberBannerAdPool()

    // New filter results start at the top, not at the old list's position.
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
                // Header (hides on scroll down, returns on scroll up)
                Column(modifier = Modifier.fillMaxWidth().background(Ink).padding(bottom = 8.dp)) {
                    val shown = viewModel.starSystems.size
                    val total = viewModel.totalSystemCount
                    TabHeader(
                        title = stringResource(R.string.star_system_list_title),
                        subtitle = when {
                            viewModel.isLoading -> stringResource(R.string.star_system_list_loading)
                            total > 0 && shown != total -> stringResource(
                                R.string.star_system_list_filtered_count, "%,d".format(shown), "%,d".format(total),
                            )
                            else -> stringResource(R.string.star_system_list_subtitle, "%,d".format(total.coerceAtLeast(shown)))
                        },
                    ) {
                        CircleIconButton(AppIcons.Search, stringResource(R.string.search_action), onOpenSearch)
                    }

                    Spacer(Modifier.height(12.dp))
                    GalaxyMapCard(onClick = onOpenGalaxyMap, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))

                    // Filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        StarSystemFilter.entries.forEach { filter ->
                            item(key = filter.name) {
                                AlmanacChip(
                                    label = stringResource(filter.labelRes),
                                    selected = viewModel.selectedFilter == filter,
                                ) { viewModel.onFilterSelected(filter) }
                            }
                        }
                    }
                }
            },
        ) { headerHeight ->
            if (viewModel.isLoading) {
                Column(Modifier.fillMaxSize().padding(top = headerHeight, start = 16.dp, end = 16.dp)) {
                    repeat(8) { SkeletonRow() }
                }
            } else if (viewModel.starSystems.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = headerHeight)) {
                    EmptyState(
                        title = stringResource(R.string.planet_list_empty_title),
                        hint = stringResource(R.string.star_system_list_empty),
                        actionLabel = stringResource(R.string.star_system_show_all),
                        onAction = { viewModel.onFilterSelected(StarSystemFilter.All) },
                    )
                }
            } else {
                val showAds = bannerAdsVisible()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = headerHeight + 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val systems = viewModel.starSystems
                    systems.forEachIndexed { index, system ->
                        item(key = system.id) {
                            val animateIn = remember {
                                index < ANIMATED_ROW_LIMIT && animatedSystemIds.add(system.id)
                            }
                            AnimatedSystemRow(
                                system = system,
                                index = index,
                                animateIn = animateIn,
                                onClick = {
                                    viewModel.trackSystemClicked(system)
                                    onSystemClick(system.id)
                                },
                            )
                        }
                        // Ad after every 5th item
                        if (showAds && (index + 1) % 5 == 0 && index < systems.size - 1) {
                            item(key = "ad_system_$index") {
                                AdBannerCard(pool = adPool, slot = index / 5)
                            }
                        }
                    }
                }
            }
        }

        BackToTopButton(
            listState = listState,
            headerState = headerState,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
        )
    }
}

/** Entry to the Galaxy Map: a surface card warmed by a star-glow gradient, with a matcha arrow. */
@Composable
private fun GalaxyMapCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .background(Brush.horizontalGradient(0.35f to Surface, 1f to StarGlow.copy(alpha = 0.85f)))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 14.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.galaxy_map_title), style = CardTitle)
            Text(stringResource(R.string.galaxy_map_entry_subtitle), style = AlmanacMeta.copy(color = InkTextDim))
        }
        CircleIconButton(AppIcons.ArrowRight, stringResource(R.string.galaxy_map_title), onClick, container = Matcha, tint = OnMatcha, size = 44.dp)
    }
}

/**
 * Rows only animate the first time they appear near the top of a list (first load or a new
 * filter result) — never when scrolled back into view, which made fast scrolls flicker.
 */
private const val ANIMATED_ROW_LIMIT = 12

@Composable
private fun AnimatedSystemRow(
    system: StarSystemSummary,
    index: Int,
    animateIn: Boolean,
    onClick: () -> Unit,
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
        },
    ) {
        StarSystemRow(system = system, onClick = onClick)
    }
}
