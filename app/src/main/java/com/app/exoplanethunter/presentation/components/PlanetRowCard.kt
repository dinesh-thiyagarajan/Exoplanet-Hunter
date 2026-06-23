package com.app.exoplanethunter.presentation.components
import com.app.exoplanethunter.presentation.preview.PreviewData
import androidx.compose.ui.tooling.preview.Preview
import com.app.exoplanethunter.presentation.preview.PreviewSurface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary

/**
 * The standard exoplanet list-row card, shared by the Planets list and the Favorites list so
 * both stay visually identical. Includes the favorite (star) toggle on the trailing edge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanetRowCard(
    planet: Exoplanet,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    isYearHighlighted: Boolean = false,
) {
    val accent = planetAccentColor(planet)
    val typeLabel = planetTypeLabel(planet)
    val typeColor = planetTypeColor(typeLabel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Accent bar drawn behind the content so the row height tracks the
                // actual (possibly wrapped) content — no IntrinsicSize clipping.
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(accent, accent.copy(alpha = 0.25f)),
                        ),
                        size = Size(4.dp.toPx(), size.height),
                    )
                }
                .padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlanetMiniRenderer(planet = planet, size = 52.dp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planet.planetName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = planet.hostName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicCyan,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TypeChip(label = typeLabel, color = typeColor)
                    planet.planetRadiusEarth?.let {
                        InfoChip("${String.format("%.1f", it)} R⊕")
                    }
                    planet.equilibriumTempK?.let {
                        InfoChip("${it.toInt()} K")
                    }
                    planet.distanceParsec?.let {
                        InfoChip("${String.format("%.0f", it)} pc")
                    }
                    InfoChip(
                        text = planet.discoveryYear.toString(),
                        isHighlighted = isYearHighlighted,
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) stringResource(R.string.favorite_remove) else stringResource(R.string.favorite_add),
                    tint = if (isFavorite) StarGold else TextMuted,
                )
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun InfoChip(text: String, isHighlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlighted) StarGold.copy(alpha = 0.2f) else SurfaceCardLight)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .then(
                if (isHighlighted) Modifier.border(
                    0.5.dp,
                    StarGold.copy(alpha = 0.5f),
                    RoundedCornerShape(6.dp),
                )
                else Modifier,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) StarGold else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
        )
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
