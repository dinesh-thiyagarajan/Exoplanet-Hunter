package com.app.exoplanethunter.presentation.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.EmptyArtSize
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.PlanetDisc
import com.app.exoplanethunter.presentation.components.SkeletonRow
import com.app.exoplanethunter.presentation.components.StarHugBadge
import com.app.exoplanethunter.presentation.components.TabHeader
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.isBanded
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.presentation.theme.Surface
import org.koin.androidx.compose.koinViewModel

/** "Your collection": the starred planets as a three-column grid of cards. */
@Composable
fun FavoritesScreen(
    onPlanetClick: (Long) -> Unit,
    onBrowsePlanets: () -> Unit = {},
    viewModel: FavoritesViewModel = koinViewModel()
) {
    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        val planets = viewModel.planets
        TabHeader(
            title = stringResource(R.string.favorites_title),
            subtitle = if (!viewModel.isLoading && planets.isNotEmpty()) stringResource(R.string.favorites_collection, planets.size) else null,
        )

        when {
            viewModel.isLoading -> Column(Modifier.padding(16.dp)) { repeat(6) { SkeletonRow() } }

            planets.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                EmptyState(
                    title = stringResource(R.string.favorites_empty_title),
                    hint = stringResource(R.string.favorites_empty_subtitle),
                    art = { StarHugBadge(size = EmptyArtSize) },
                    actionLabel = stringResource(R.string.favorites_browse),
                    onAction = onBrowsePlanets,
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 108.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(planets, key = { it.id }) { planet ->
                    FavoriteCard(
                        planet = planet,
                        onClick = { onPlanetClick(planet.id) },
                        onUnfavorite = { viewModel.toggleFavorite(planet) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun FavoriteCard(planet: Exoplanet, onClick: () -> Unit, onUnfavorite: () -> Unit) {
    val bucket = TempBucket.of(planet.estimatedTempK())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .background(Surface)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlanetDisc(color = bucket.color, size = 64.dp, banded = planet.isBanded())
            Spacer(Modifier.height(10.dp))
            Text(
                planet.planetName,
                fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkText,
                maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(bucket.label, fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = bucket.color)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .clip(CircleShape)
                .clickable(role = Role.Checkbox, onClick = onUnfavorite),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.StarFilled, stringResource(R.string.favorite_remove), tint = StarOrange, modifier = Modifier.size(18.dp))
        }
    }
}
