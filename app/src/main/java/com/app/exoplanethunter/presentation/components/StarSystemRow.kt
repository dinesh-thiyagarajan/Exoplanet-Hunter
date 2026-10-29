package com.app.exoplanethunter.presentation.components

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.RowName
import com.app.exoplanethunter.presentation.theme.StarClassF
import com.app.exoplanethunter.presentation.theme.StarClassG
import com.app.exoplanethunter.presentation.theme.StarClassHot
import com.app.exoplanethunter.presentation.theme.StarClassK
import com.app.exoplanethunter.presentation.theme.StarClassM
import com.app.exoplanethunter.presentation.theme.StarOrange

/** A star's colour from its spectral class, e.g. M → deep orange, G → warm white. */
fun starClassColor(spectralType: String?): Color =
    when (spectralType?.trim()?.firstOrNull()?.uppercaseChar()) {
        'M' -> StarClassM
        'K' -> StarClassK
        'G' -> StarClassG
        'F' -> StarClassF
        'A', 'B', 'O' -> StarClassHot
        else -> StarOrange
    }

/** "Single", "Binary", "Trinary" or "4 stars". */
fun multiplicityLabel(context: Context, numStars: Int): String = when (numStars) {
    0, 1 -> context.getString(R.string.star_system_multiplicity_single_short)
    2 -> context.getString(R.string.star_system_multiplicity_binary)
    3 -> context.getString(R.string.star_system_multiplicity_trinary)
    else -> context.getString(R.string.star_system_multiplicity_many, numStars)
}

/**
 * Star-system row: a star glyph coloured by spectral class (with a companion dot for
 * binaries and trinaries), the name, "M8 V · Single" and one matcha dot per planet, and
 * the distance over the planet count.
 */
@Composable
fun StarSystemRow(system: StarSystemSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val color = starClassColor(system.spectralType)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StarGlyph(
            color = color,
            size = 28.dp,
            companion = if (system.numStars >= 2) StarOrange else null,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(system.hostName, style = RowName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val meta = listOfNotNull(system.spectralType?.takeIf { it.isNotBlank() }, multiplicityLabel(context, system.numStars))
                    .joinToString(" · ")
                Text(meta, style = AlmanacMeta, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(system.numPlanets.coerceIn(0, 10)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Matcha))
                    }
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(formatLightYears(system.distanceParsec), style = AlmanacData.copy(fontSize = 14.sp))
            Spacer(Modifier.height(4.dp))
            Text(
                context.resources.getQuantityString(R.plurals.planet_count, system.numPlanets, system.numPlanets),
                style = AlmanacMeta.copy(fontSize = 12.sp),
            )
        }
    }
}
