package com.app.exoplanethunter.presentation.screens.planetdetail
import androidx.compose.ui.tooling.preview.Preview
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.ads.AdBannerCard
import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.app.exoplanethunter.presentation.components.Planet3DRenderer
import com.app.exoplanethunter.presentation.components.ReticleOverlay
import com.app.exoplanethunter.presentation.components.SkyChartCard
import com.app.exoplanethunter.presentation.components.graticule
import com.app.exoplanethunter.presentation.components.catalogueId
import com.app.exoplanethunter.presentation.components.isLikelyTidallyLocked
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CautionYellow
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.HabitableGreen
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.TextMuted
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlanetDetailScreen(
    planetId: Long,
    onBack: () -> Unit,
    viewModel: PlanetDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(planetId) {
        viewModel.loadPlanet(planetId)
    }
    PlanetDetailContent(
        state = viewModel.uiState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@Composable
fun PlanetDetailContent(
    state: PlanetDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brass)
            }
        } else {
            val planet = state.planet ?: return@Box
            val insight = state.insight
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Top bar (pinned)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceBlack.copy(alpha = 0.85f))
                        .padding(top = 44.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = InkText,
                        )
                    }
                    Text(
                        text = catalogueId(planet.id),
                        style = AlmanacMeta,
                        modifier = Modifier.weight(1f),
                    )
                    val copiedMessage = stringResource(R.string.planet_detail_name_copied)
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(planet.planetName))
                            Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.planet_detail_copy_name),
                            tint = InkTextDim,
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (state.isFavorite) stringResource(R.string.favorite_remove) else stringResource(R.string.favorite_add),
                            tint = if (state.isFavorite) Brass else InkTextDim,
                        )
                    }
                }

                // Scrollable content below the pinned top bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    val tidallyLocked = remember(planet) { isLikelyTidallyLocked(planet) }

                    // 3D Planet, sighted in a brass reticle over a graticule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .graticule(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ReticleOverlay(modifier = Modifier.size(290.dp))
                        Planet3DRenderer(
                            planet = planet,
                            size = 240.dp,
                            enableRotation = true,
                            autoRotate = true,
                            tidallyLocked = tidallyLocked,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = planet.planetName,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 24.dp),
                    )
                    Text(
                        text = stringResource(R.string.planet_detail_subtitle, planet.hostName),
                        style = AlmanacCaption,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.planet_detail_drag_to_rotate),
                        style = AlmanacMeta,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    if (tidallyLocked) {
                        TidalLockBadge(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Classification badge
                    insight?.let { ins ->
                        ClassificationBadge(
                            classification = ins.classification,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 16.dp),
                        )
                    }

                    // Content
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Verdict instrument: temperature verdict + Earth-similarity figure
                        VerdictInstrument(planet = planet, insight = insight)

                        // ML Habitability Insight
                        insight?.let { ins ->
                            AnimatedSection(delay = 0) {
                                HabitabilityCard(insight = ins)
                            }
                        }

                        // Planet Properties
                        AnimatedSection(delay = 100) {
                            PlanetPropertiesCard(planet = planet)
                        }

                        // Compared to Earth
                        if (planet.hasEarthComparisonData()) {
                            AnimatedSection(delay = 150) {
                                EarthComparisonCard(planet = planet)
                            }
                        }

                        // Ad banner between property cards
                        AdBannerCard()

                        // Stellar Properties
                        AnimatedSection(delay = 200) {
                            StellarPropertiesCard(planet = planet)
                        }

                        // Discovery Info
                        AnimatedSection(delay = 300) {
                            DiscoveryCard(planet = planet)
                        }

                        // Where to find the host star in the sky
                        AnimatedSection(delay = 350) {
                            SkyChartCard(
                                ra = planet.ra,
                                dec = planet.dec,
                                hostName = planet.hostName
                            )
                        }

                        // ML Insights
                        insight?.let { ins ->
                            if (ins.insights.isNotEmpty()) {
                                AnimatedSection(delay = 400) {
                                    InsightsCard(insights = ins.insights)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedSection(delay: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            initialOffsetY = { it / 4 },
        ),
    ) {
        content()
    }
}

@Composable
private fun ClassificationBadge(
    classification: PlanetClassification,
    modifier: Modifier = Modifier,
) {
    val color = when (classification) {
        PlanetClassification.POTENTIALLY_HABITABLE -> HabitableGreen
        PlanetClassification.ROCKY -> CosmicCyan
        PlanetClassification.SUPER_EARTH -> AuroraGreen
        PlanetClassification.SUB_EARTH -> CautionYellow
        PlanetClassification.NEPTUNE_LIKE -> NebulaPink
        PlanetClassification.GAS_GIANT -> SolarOrange
        PlanetClassification.UNKNOWN -> TextMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = classification.label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun TidalLockBadge(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SolarOrange.copy(alpha = 0.15f))
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.tidal_lock_badge),
                style = MaterialTheme.typography.labelLarge,
                color = SolarOrange,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = stringResource(R.string.tidal_lock_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 32.dp, end = 32.dp),
        )
    }
}


@Preview
@Composable
private fun ClassificationBadgePreview() = PreviewSurface {
    ClassificationBadge(classification = PlanetClassification.POTENTIALLY_HABITABLE)
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
