package com.workspace.exoplanethunter.presentation.screens.starsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.exoplanethunter.presentation.components.StarField
import com.workspace.exoplanethunter.presentation.theme.CosmicCyan
import com.workspace.exoplanethunter.presentation.theme.NebulaPink
import com.workspace.exoplanethunter.presentation.theme.SolarOrange
import com.workspace.exoplanethunter.presentation.theme.SpaceBlack
import com.workspace.exoplanethunter.presentation.theme.StarGold
import com.workspace.exoplanethunter.presentation.theme.SurfaceCard
import com.workspace.exoplanethunter.presentation.theme.SurfaceCardLight
import com.workspace.exoplanethunter.presentation.theme.TextMuted
import com.workspace.exoplanethunter.presentation.theme.TextSecondary
import com.workspace.exoplanethunter.ads.AdBannerCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarSystemListScreen(
    onSystemClick: (String) -> Unit,
    viewModel: StarSystemListViewModel = viewModel(factory = StarSystemListViewModel.Factory)
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
                    .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Star Systems",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(StarGold, SolarOrange)
                        )
                    )
                )

                Text(
                    text = if (viewModel.isLoading) "Loading..."
                    else "Explore ${viewModel.starSystems.size} star systems",
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
                        Text("Search star systems...", color = TextMuted)
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
                        cursorColor = StarGold,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !viewModel.showMultiPlanetOnly,
                        onClick = {
                            if (viewModel.showMultiPlanetOnly) viewModel.onToggleMultiPlanet()
                        },
                        label = { Text("All Systems", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (!viewModel.showMultiPlanetOnly) SpaceBlack else StarGold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = StarGold,
                            selectedLabelColor = SpaceBlack
                        )
                    )

                    FilterChip(
                        selected = viewModel.showMultiPlanetOnly,
                        onClick = {
                            if (!viewModel.showMultiPlanetOnly) viewModel.onToggleMultiPlanet()
                        },
                        label = { Text("Multi-Planet", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (viewModel.showMultiPlanetOnly) SpaceBlack else NebulaPink
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = NebulaPink,
                            selectedLabelColor = SpaceBlack
                        )
                    )
                }
            }

            // System list
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StarGold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val systems = viewModel.starSystems
                    systems.forEachIndexed { index, hostName ->
                        item(key = hostName) {
                            AnimatedSystemCard(
                                hostName = hostName,
                                index = index,
                                onClick = { onSystemClick(hostName) }
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
    hostName: String,
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
        StarSystemCard(hostName = hostName, onClick = onClick)
    }
}

@Composable
private fun StarSystemCard(
    hostName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            // Star icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                StarGold.copy(alpha = 0.6f),
                                SolarOrange.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hostName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to explore system",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Arrow indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCardLight)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "View",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicCyan,
                    fontSize = 11.sp
                )
            }
        }
    }
}
