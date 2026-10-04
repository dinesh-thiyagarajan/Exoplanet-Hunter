package com.app.exoplanethunter.presentation.screens.starsystem

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.bannerAdsVisible
import com.app.exoplanethunter.ads.rememberBannerAdPool
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.components.BackToTopButton
import com.app.exoplanethunter.presentation.components.CollapsingHeaderLayout
import com.app.exoplanethunter.presentation.components.rememberCollapsingHeaderState
import com.app.exoplanethunter.presentation.components.formatLightYears
import com.app.exoplanethunter.presentation.components.starTypeLabel
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.Surface as SurfaceColor
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarSystemListScreen(
    onSystemClick: (Long) -> Unit,
    onOpenGalaxyMap: () -> Unit = {},
    viewModel: StarSystemListViewModel = koinViewModel(),
) {
    val listState = rememberLazyListState()
    val animatedSystemIds = remember { mutableSetOf<Long>() }
    val headerState = rememberCollapsingHeaderState()
    val adPool = rememberBannerAdPool()

    // New filter / search results start at the top, not at the old list's position.
    var handledScrollReset by rememberSaveable { mutableIntStateOf(viewModel.scrollResetCount) }
    LaunchedEffect(viewModel.scrollResetCount) {
        if (viewModel.scrollResetCount != handledScrollReset) {
            handledScrollReset = viewModel.scrollResetCount
            listState.scrollToItem(0)
            headerState.expand()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
    ) {
        CollapsingHeaderLayout(
            state = headerState,
            header = {
                // Header (hides on scroll down, returns on scroll up)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceBlack)
                        .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text(stringResource(R.string.star_system_list_eyebrow), style = AlmanacEyebrow)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.star_system_list_title),
                                style = MaterialTheme.typography.displayMedium,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .border(0.5.dp, Brass, RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenGalaxyMap)
                                .padding(10.dp),
                        ) {
                            Icon(
                                Icons.Default.Explore,
                                contentDescription = stringResource(R.string.galaxy_map_title),
                                tint = Brass,
                            )
                        }
                    }

                    Text(
                        text = if (viewModel.isLoading) stringResource(R.string.star_system_list_loading)
                        else if (viewModel.totalSystemCount > 0 && viewModel.starSystems.size != viewModel.totalSystemCount) {
                            stringResource(
                                R.string.star_system_list_filtered_count,
                                "%,d".format(viewModel.starSystems.size),
                                "%,d".format(viewModel.totalSystemCount),
                            )
                        } else stringResource(R.string.star_system_list_count, viewModel.starSystems.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search bar
                    TextField(
                        value = viewModel.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = {
                            Text(stringResource(R.string.star_system_list_search_hint), color = TextMuted)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_clear),
                                        tint = TextMuted,
                                    )
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
                            unfocusedTextColor = InkText,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp),
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
            // System list
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = headerHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Brass)
                }
            } else {
                val showAds = bannerAdsVisible()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = headerHeight + 8.dp,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val systems = viewModel.starSystems
                    systems.forEachIndexed { index, system ->
                        item(key = system.id) {
                            val animateIn = remember {
                                index < ANIMATED_ROW_LIMIT && animatedSystemIds.add(system.id)
                            }
                            AnimatedSystemCard(
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        )
    }
}

/**
 * Rows only animate the first time they appear near the top of a list (first load or a new
 * filter/search result) — never when scrolled back into view, which made fast scrolls flicker.
 */
private const val ANIMATED_ROW_LIMIT = 12

@Composable
private fun AnimatedSystemCard(
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
        modifier = Modifier
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 24f
            },
    ) {
        StarSystemCard(system = system, onClick = onClick)
    }
}

/**
 * A star-system row in the same almanac plate style as [PlanetRowCard]: a brass star disc,
 * the host name in serif, a mono line (spectral type · multiplicity), and distance with the
 * planet count on the right.
 */
@Composable
private fun StarSystemCard(
    system: StarSystemSummary,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val spectral = system.spectralType?.trim()?.takeIf { it.isNotBlank() }
        ?: starTypeLabel(system.spectralType)
    val multiplicity = starCountLabel(context, system.numStars)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceColor)
            .border(0.5.dp, Hairline, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(start = 16.dp, top = 13.dp, bottom = 13.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Brass star disc with a faint inner light.
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Brass, Brass.copy(alpha = 0.45f)),
                        center = Offset(11f, 11f),
                        radius = 38f,
                    )
                )
                .border(0.5.dp, Hairline, CircleShape),
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = system.hostName,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${spectral.uppercase()} · ${multiplicity.uppercase()}",
                style = AlmanacMeta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatLightYears(system.distanceParsec), style = AlmanacData)
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = pluralStringResource(R.plurals.planet_count, system.numPlanets, system.numPlanets).uppercase(),
                style = AlmanacMeta.copy(color = Brass),
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = InkTextFaint,
            modifier = Modifier.padding(start = 6.dp).size(18.dp),
        )
    }
}

private fun starCountLabel(context: android.content.Context, numStars: Int): String = when (numStars) {
    0, 1 -> context.getString(R.string.star_system_multiplicity_single)
    2 -> context.getString(R.string.star_system_multiplicity_binary)
    3 -> context.getString(R.string.star_system_multiplicity_trinary)
    else -> context.getString(R.string.star_system_multiplicity_many, numStars)
}
