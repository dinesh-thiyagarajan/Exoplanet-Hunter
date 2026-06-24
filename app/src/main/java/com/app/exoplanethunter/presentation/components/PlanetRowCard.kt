package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Surface
import com.app.exoplanethunter.presentation.theme.SurfaceRaised

/**
 * The catalogue list-row, shared by the Planets list and Favorites. An almanac
 * plate: a temperature-tinted disc, the planet name in serif, a mono catalogue
 * line (id · star class · classification), distance in light-years, and a
 * temperature dot. Selected rows gain a brass left rule and a raised surface.
 */
@Composable
fun PlanetRowCard(
    planet: Exoplanet,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    isYearHighlighted: Boolean = false,
    isSelected: Boolean = false,
) {
    val discColor = planetAccentColor(planet)
    val tempColor = temperatureColor(planet.equilibriumTempK)
    val tempLabel = temperatureLabel(planet.equilibriumTempK)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SurfaceRaised else Surface)
            .border(
                width = 0.5.dp,
                color = if (isSelected) Brass else Hairline,
                shape = RoundedCornerShape(6.dp),
            )
            .clickable { onClick() }
            // Brass left rule for the selected entry.
            .drawBehind {
                if (isSelected) {
                    drawRect(color = Brass, size = Size(3.dp.toPx(), size.height), topLeft = Offset.Zero)
                }
            }
            .padding(start = 16.dp, top = 13.dp, bottom = 13.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Temperature-tinted disc with a faint inner light.
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(discColor, discColor.copy(alpha = 0.65f)),
                        center = Offset(11f, 11f),
                        radius = 38f,
                    )
                )
                .border(0.5.dp, Hairline, CircleShape),
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = planet.planetName,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${catalogueId(planet.id)} · ${starTypeLabel(planet.spectralType)} · ${classificationLabel(planet)}",
                style = AlmanacMeta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatLightYears(planet.distanceParsec), style = AlmanacData)
            Spacer(modifier = Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(tempColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = tempLabel, style = AlmanacMeta.copy(color = tempColor))
            }
        }

        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isFavorite) stringResource(R.string.favorite_remove) else stringResource(R.string.favorite_add),
                tint = if (isFavorite) Brass else InkTextFaint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PlanetRowCardPreview() = PreviewSurface {
    PlanetRowCard(
        planet = PreviewData.planet,
        isFavorite = true,
        onToggleFavorite = {},
        onClick = {},
    )
}
