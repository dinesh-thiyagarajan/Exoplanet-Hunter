package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.app.exoplanethunter.R
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkText
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    else stringResource(R.string.star_system_list_count, viewModel.starSystems.size),
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
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
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

            // System list
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Brass)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val systems = viewModel.starSystems
                    systems.forEachIndexed { index, system ->
                        item(key = system.id) {
                            AnimatedSystemCard(
                                system = system,
                                index = index,
                                onClick = {
                                    viewModel.trackSystemClicked(system)
                                    onSystemClick(system.id)
                                },
                            )
                        }
                        // Ad after every 5th item
                        if ((index + 1) % 5 == 0 && index < systems.size - 1) {
                            item(key = "ad_system_$index") {
                                AdBannerCard()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedSystemCard(
    system: StarSystemSummary,
    index: Int,
    onClick: () -> Unit,
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
            },
    ) {
        StarSystemCard(system = system, onClick = onClick)
    }
}

@Composable
private fun StarSystemCard(
    system: StarSystemSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Star icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                StarGold.copy(alpha = 0.6f),
                                SolarOrange.copy(alpha = 0.2f),
                                Color.Transparent,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = system.hostName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                system.spectralType?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = stringResource(R.string.star_system_spectral_type, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = StarGold,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val planetCount = system.numPlanets
                    SystemInfoChip(
                        text = pluralStringResource(R.plurals.planet_count, planetCount, planetCount),
                        color = CosmicCyan,
                    )
                    SystemInfoChip(text = starCountLabel(LocalContext.current, system.numStars), color = SolarOrange)
                    system.distanceParsec?.let { dist ->
                        SystemInfoChip(text = "${String.format("%.0f", dist)} pc", color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
            )
        }
    }
}

private fun starCountLabel(context: android.content.Context, numStars: Int): String = when (numStars) {
    0, 1 -> context.getString(R.string.star_system_multiplicity_single)
    2 -> context.getString(R.string.star_system_multiplicity_binary)
    3 -> context.getString(R.string.star_system_multiplicity_trinary)
    else -> context.getString(R.string.star_system_multiplicity_many, numStars)
}

@Composable
private fun SystemInfoChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceCardLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
