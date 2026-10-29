package com.app.exoplanethunter.presentation.screens.planetdetail

import android.app.Activity
import android.widget.Toast
import com.app.exoplanethunter.review.AppReviewManager
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BareIconButton
import com.app.exoplanethunter.presentation.components.SizeToScaleCard
import com.app.exoplanethunter.presentation.components.SkyChartCard
import com.app.exoplanethunter.presentation.components.isLikelyTidallyLocked
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.starClassColor
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.StarOrange
import androidx.compose.material3.MaterialTheme
import com.app.exoplanethunter.presentation.components.SkeletonRow
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlanetDetailScreen(
    planetId: Long,
    onBack: () -> Unit,
    onOpenSystem: (Long) -> Unit = {},
    viewModel: PlanetDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(planetId) {
        viewModel.loadPlanet(planetId)
    }
    // Ask for a review (at most every 60 days) only after a few seconds with a loaded planet,
    // when the user is engaged rather than on their way somewhere.
    val activity = LocalContext.current as? Activity
    val loaded = viewModel.uiState.planet != null
    LaunchedEffect(planetId, loaded) {
        if (!loaded || activity == null) return@LaunchedEffect
        delay(REVIEW_DELAY_MILLIS)
        AppReviewManager.maybeRequestReview(activity)
    }
    PlanetDetailContent(
        state = viewModel.uiState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onOpenSystem = onOpenSystem,
    )
}

private const val REVIEW_DELAY_MILLIS = 6_000L

@Composable
fun PlanetDetailContent(
    state: PlanetDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSystem: (Long) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize().background(Ink)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar (pinned): back, copy name, favourite.
            val planet = state.planet
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topBarInsets()
                    .padding(top = 4.dp, bottom = 4.dp, start = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BareIconButton(AppIcons.Back, stringResource(R.string.cd_back), onBack)
                Spacer(Modifier.weight(1f))
                if (planet != null) {
                    val copiedMessage = stringResource(R.string.planet_detail_name_copied)
                    BareIconButton(
                        AppIcons.Copy,
                        stringResource(R.string.planet_detail_copy_name),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(planet.planetName))
                            Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                        },
                        tint = InkTextDim,
                    )
                    BareIconButton(
                        if (state.isFavorite) AppIcons.StarFilled else AppIcons.Star,
                        stringResource(if (state.isFavorite) R.string.favorite_remove else R.string.favorite_add),
                        onClick = onToggleFavorite,
                        tint = if (state.isFavorite) StarOrange else InkTextDim,
                    )
                }
            }

            if (state.isLoading) {
                Column(Modifier.padding(16.dp)) { repeat(6) { SkeletonRow() } }
                return@Column
            }
            planet ?: return@Column
            val insight = state.insight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .screenContentInsets()
                    .verticalScroll(rememberScrollState()),
            ) {
                val tidallyLocked = remember(planet) { isLikelyTidallyLocked(planet) }
                PlanetHero(planet = planet, tidallyLocked = tidallyLocked)

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Name and "A world orbiting ● K2-18" (the star opens its system).
                    Column {
                        Text(planet.planetName, style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.planet_detail_subtitle), fontFamily = BodyFamily, fontSize = 16.sp, color = InkText)
                            Spacer(Modifier.width(8.dp))
                            Row(
                                modifier = Modifier
                                    .clip(Corners.Pill)
                                    .clickable(enabled = planet.systemId != 0L) { onOpenSystem(planet.systemId) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(9.dp).clip(CircleShape).background(starClassColor(planet.spectralType)))
                                Spacer(Modifier.width(6.dp))
                                Text(planet.hostName, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Matcha)
                            }
                        }
                    }

                    PlanetSummaryCard(planet = planet, insight = insight)
                    HeroFigureGrid(planet = planet)

                    Section(delay = 0) {
                        Column {
                            SectionHeading(stringResource(R.string.size_scale_title))
                            Spacer(Modifier.height(12.dp))
                            SizeToScaleCard(planets = listOf(planet))
                        }
                    }

                    insight?.let { ins -> Section(delay = 60) { HabitabilityCard(planet = planet, insight = ins) } }

                    if (planet.hasEarthComparisonData()) {
                        Section(delay = 120) { EarthComparisonSection(planet = planet) }
                    }

                    Section(delay = 160) {
                        HostStarCard(
                            planet = planet,
                            onOpenSystem = if (planet.systemId != 0L) ({ onOpenSystem(planet.systemId) }) else null,
                        )
                    }

                    Section(delay = 200) { PropertiesSection(planet = planet) }

                    AdBannerCard()

                    Section(delay = 240) { DiscoverySection(planet = planet) }

                    Section(delay = 280) {
                        SkyChartCard(ra = planet.ra, dec = planet.dec, hostName = planet.hostName)
                    }

                    insight?.takeIf { it.insights.isNotEmpty() }?.let { ins ->
                        Section(delay = 320) { InsightsCard(insights = ins.insights) }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun Section(delay: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            initialOffsetY = { it / 6 },
        ),
    ) {
        Box(Modifier.padding(top = 16.dp)) { content() }
    }
}

@Preview
@Composable
private fun PlanetDetailContentLoadedPreview() = PreviewSurface {
    PlanetDetailContent(
        state = PlanetDetailUiState(
            isLoading = false,
            planet = PreviewData.planet,
            insight = PreviewData.insight,
            isFavorite = true,
        ),
        onBack = {},
        onToggleFavorite = {},
    )
}

@Preview
@Composable
private fun PlanetDetailContentLoadingPreview() = PreviewSurface {
    PlanetDetailContent(
        state = PlanetDetailUiState(isLoading = true),
        onBack = {},
        onToggleFavorite = {},
    )
}
