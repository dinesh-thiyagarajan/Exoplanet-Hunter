package com.app.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.components.RealisticPlanet
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacCaption

/**
 * The planet as it most plausibly looks: a draggable render on a soft glow in its
 * temperature colour, with an honest caption that it's an impression.
 */
@Composable
internal fun PlanetHero(planet: Exoplanet, tidallyLocked: Boolean, modifier: Modifier = Modifier) {
    val glow = remember(planet) { TempBucket.of(planet.estimatedTempK()).color }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(listOf(glow.copy(alpha = 0.22f), Color.Transparent)),
                        radius = size.minDimension / 2,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            RealisticPlanet(planet = planet, size = 230.dp, tidallyLocked = tidallyLocked)
        }
        Text(stringResource(R.string.hero_impression_caption), style = AlmanacCaption)
        Spacer(Modifier.height(4.dp))
    }
}

@Preview
@Composable
private fun PlanetHeroPreview() = PreviewSurface {
    PlanetHero(planet = PreviewData.planet, tidallyLocked = false)
}
