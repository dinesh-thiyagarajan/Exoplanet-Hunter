package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.formatLightYears
import com.app.exoplanethunter.presentation.components.multiplicityLabel
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.SkyChartCard
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.TextMuted
import org.koin.androidx.compose.koinViewModel

@Composable
fun StarSystemDetailScreen(
    systemId: Long,
    onPlanetClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StarSystemDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(systemId) {
        viewModel.loadSystem(systemId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
    ) {
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = com.app.exoplanethunter.presentation.theme.Matcha)
            }
        } else if (viewModel.starSystem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.star_system_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            val system = viewModel.starSystem!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .screenContentInsets()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Top bar: back, the system's name, and what it is.
                val context = LocalContext.current
                BackTopBar(
                    onBack = onBack,
                    backDescription = stringResource(R.string.cd_back),
                    title = system.hostName,
                    titleSize = 30,
                    subtitle = listOfNotNull(
                        pluralStringResource(R.plurals.planet_count, system.numPlanets, system.numPlanets),
                        multiplicityLabel(context, system.numStars).lowercase().let {
                            if (system.numStars <= 1) stringResource(R.string.star_system_single_star_lower) else it
                        },
                        system.distanceParsec?.let { formatLightYears(it) },
                    ).joinToString(" · "),
                    modifier = Modifier.statusBarsPadding(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                SolarSystemVisualization(
                    system = system,
                    onPlanetClick = onPlanetClick,
                    onToggle = viewModel::onOrreryToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Cards section
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AnimatedSection(delayMs = 0) {
                        StellarInfoCard(system = system)
                    }
                    AnimatedSection(delayMs = 100) {
                        SkyChartCard(
                            ra = system.ra,
                            dec = system.dec,
                            hostName = system.hostName,
                        )
                    }
                    AnimatedSection(delayMs = 150) {
                        PlanetsInfoCard(
                            planets = system.planets,
                            onPlanetClick = onPlanetClick,
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
