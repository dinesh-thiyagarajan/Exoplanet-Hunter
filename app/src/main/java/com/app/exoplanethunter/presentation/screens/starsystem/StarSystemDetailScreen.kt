package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.app.exoplanethunter.R
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary

@Composable
fun StarSystemDetailScreen(
    systemId: Long,
    onPlanetClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StarSystemDetailViewModel = koinViewModel()
) {
    LaunchedEffect(systemId) {
        viewModel.loadSystem(systemId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        StarField(starCount = 80)

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StarGold)
            }
        } else if (viewModel.starSystem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.star_system_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            val system = viewModel.starSystem!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = system.hostName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val planetsText = pluralStringResource(
                            R.plurals.planet_count, system.numPlanets, system.numPlanets
                        )
                        val systemDesc = if (system.numStars > 1) {
                            "$planetsText \u2022 " +
                                stringResource(R.string.star_system_star_count, system.numStars)
                        } else planetsText
                        Text(
                            text = systemDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // System type badge
                    val badgeText = when {
                        system.numStars >= 3 -> stringResource(R.string.star_system_multiplicity_trinary)
                        system.numStars == 2 -> stringResource(R.string.star_system_multiplicity_binary)
                        else -> pluralStringResource(R.plurals.planet_count, system.numPlanets, system.numPlanets)
                    }
                    val badgeColor = when {
                        system.numStars >= 3 -> NebulaPink
                        system.numStars == 2 -> SolarOrange
                        else -> StarGold
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Solar system visualization — fills most of the screen
                SolarSystemVisualization(
                    system = system,
                    onPlanetClick = onPlanetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(horizontal = 4.dp)
                )

                Text(
                    text = stringResource(R.string.star_system_tap_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                // Cards section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedSection(delayMs = 0) {
                        StellarInfoCard(system = system)
                    }
                    AnimatedSection(delayMs = 150) {
                        PlanetsInfoCard(
                            planets = system.planets,
                            onPlanetClick = onPlanetClick
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
