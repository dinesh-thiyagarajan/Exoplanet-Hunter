package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacSectionLabel
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import kotlin.math.max

/**
 * Planets drawn to scale beside Earth — and Neptune and Jupiter once one of them is big
 * enough for those to be the useful yardstick. Planets without a known radius are skipped;
 * nothing is drawn if none has one.
 */
@Composable
fun SizeToScaleCard(planets: List<Exoplanet>, modifier: Modifier = Modifier) {
    val sized = planets.filter { (it.planetRadiusEarth ?: 0.0) > 0.0 }
    if (sized.isEmpty()) return
    val biggest = sized.maxOf { it.planetRadiusEarth!! }
    val bodies = buildList {
        add(ScaleBody(stringResource(R.string.size_scale_earth), 1.0, Color(0xFF3F7FB5)))
        if (biggest > 2.5) add(ScaleBody(stringResource(R.string.size_scale_neptune), NEPTUNE_RADIUS_EARTH, Color(0xFF4A7FD0)))
        if (biggest > 6.0) add(ScaleBody(stringResource(R.string.size_scale_jupiter), JUPITER_RADIUS_EARTH, Color(0xFFD2B48C)))
        sized.forEach { add(ScaleBody(it.planetName, it.planetRadiusEarth!!, it.surfaceKind().swatch(), highlight = true)) }
    }.sortedBy { it.radiusEarth }
    val largest = bodies.maxOf { it.radiusEarth }
    val maxDiameter = 84.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Hairline, RoundedCornerShape(8.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.size_scale_title), style = AlmanacSectionLabel, modifier = Modifier.weight(1f))
            // With a single planet, spell out the one ratio that matters.
            sized.singleOrNull()?.let {
                Text(
                    text = stringResource(R.string.size_scale_ratio, "%.2f".format(it.planetRadiusEarth)),
                    style = AlmanacMeta.copy(color = Brass),
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            bodies.forEach { body ->
                val d = max(3f, (maxDiameter.value * body.radiusEarth / largest).toFloat()).dp
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(d)
                            .clip(CircleShape)
                            .drawBehind {
                                // Lit from the upper left, like the rendered planets.
                                drawCircle(
                                    Brush.radialGradient(
                                        listOf(body.color, body.color.copy(alpha = 0.45f)),
                                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                                        radius = size.width * 1.1f,
                                    )
                                )
                            }
                            .then(if (body.highlight) Modifier.border(1.dp, Brass, CircleShape) else Modifier),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body.label,
                        style = AlmanacMeta.copy(color = if (body.highlight) Brass else InkTextFaint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private data class ScaleBody(
    val label: String,
    val radiusEarth: Double,
    val color: Color,
    val highlight: Boolean = false,
)

@Preview
@Composable
private fun SizeToScaleCardPreview() = PreviewSurface {
    SizeToScaleCard(planets = listOf(PreviewData.planet, PreviewData.hotPlanet), modifier = Modifier.padding(20.dp))
}
