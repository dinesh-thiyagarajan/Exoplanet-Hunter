package com.app.exoplanethunter.presentation.screens.favorites

import androidx.compose.ui.res.stringResource
import com.app.exoplanethunter.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.ads.bannerAdsVisible
import com.app.exoplanethunter.ads.rememberBannerAdPool
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.InkTextDim

@Composable
fun FavoritesScreen(
    onPlanetClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel()
) {
    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
            ) {
                Text(stringResource(R.string.favorites_eyebrow), style = AlmanacEyebrow)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = stringResource(R.string.favorites_count, viewModel.planets.size),
                    style = AlmanacMeta,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when {
                viewModel.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Brass)
                    }
                }

                viewModel.planets.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = InkTextFaint,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.favorites_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = InkTextDim,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.favorites_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkTextFaint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val showAds = bannerAdsVisible()
                    val adPool = rememberBannerAdPool()
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val planets = viewModel.planets
                        planets.forEachIndexed { index, planet ->
                            item(key = planet.id) {
                                PlanetRowCard(
                                    planet = planet,
                                    isFavorite = true,
                                    onToggleFavorite = { viewModel.toggleFavorite(planet) },
                                    onClick = { onPlanetClick(planet.id) }
                                )
                            }
                            // Interleave an ad after every 5th favorite (not at the end of the list).
                            if (showAds && (index + 1) % 5 == 0 && index < planets.size - 1) {
                                item(key = "ad_fav_$index") {
                                    AdBannerCard(pool = adPool, slot = index / 5)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
