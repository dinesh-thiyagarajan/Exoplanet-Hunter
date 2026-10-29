package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.preview.PreviewData
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.RowName
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.presentation.theme.Surface

/** Whether a planet is drawn with gas-giant bands. */
fun Exoplanet.isBanded(): Boolean = (planetRadiusEarth ?: 0.0) >= 6.0

/** The catalogue line under a planet's name, e.g. "EH-0412 · M-type · Sub-Neptune". */
fun Exoplanet.catalogueLine(): String =
    listOfNotNull(catalogueId(id), starTypeLabel(spectralType), classificationLabel(this)).joinToString(" · ")

/**
 * The planet list row, shared by Planets, Search and Compare mode: a 40 dp disc in the
 * temperature colour, the name and catalogue line, the distance over the temperature word,
 * and the favourite star. Selected rows (compare mode) sit on a surface pill with a matcha ring.
 */
@Composable
fun PlanetRowCard(
    planet: Exoplanet,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    isYearHighlighted: Boolean = false,
    isSelected: Boolean = false,
    secondaryLine: String = planet.catalogueLine(),
) {
    // Measured where the catalogue has it, otherwise estimated from the star and orbit —
    // the same reading the detail screen shows, so the two never disagree.
    val bucket = TempBucket.of(planet.estimatedTempK())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .then(
                if (isSelected) Modifier.background(Surface).border(2.dp, Matcha, Corners.SmallCard)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(start = if (isSelected) 12.dp else 0.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlanetDisc(color = bucket.color, size = 40.dp, banded = planet.isBanded())

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = planet.planetName,
                style = RowName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isYearHighlighted) "$secondaryLine · ${planet.discoveryYear}" else secondaryLine,
                style = AlmanacMeta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatLightYears(planet.distanceParsec), style = AlmanacData.copy(fontSize = 14.sp))
            Spacer(modifier = Modifier.height(4.dp))
            TempIndicator(bucket)
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Checkbox, onClick = onToggleFavorite),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isFavorite) AppIcons.StarFilled else AppIcons.Star,
                contentDescription = if (isFavorite) stringResource(R.string.favorite_remove) else stringResource(R.string.favorite_add),
                tint = if (isFavorite) StarOrange else InkTextFaint,
                modifier = Modifier.size(22.dp),
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
