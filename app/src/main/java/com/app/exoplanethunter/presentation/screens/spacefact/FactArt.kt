package com.app.exoplanethunter.presentation.screens.spacefact

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.PlanetDisc
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.MatchaMid
import com.app.exoplanethunter.presentation.theme.StarDeep
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.presentation.theme.TempCold
import com.app.exoplanethunter.presentation.theme.TempHotColor
import com.app.exoplanethunter.presentation.theme.TempScorched
import com.app.exoplanethunter.presentation.theme.Well

/** Each fact gets a stable colour from the app's palette, so the library reads as a set. */
private val FactPalette = listOf(StarOrange, TempScorched, StarDeep, TempCold, TempHotColor, MatchaMid)

internal fun factColor(id: Int): Color = FactPalette[Math.floorMod(id, FactPalette.size)]
internal fun factAccent(id: Int): Color = FactPalette[Math.floorMod(id * 7 + 3, FactPalette.size)]

/** The library's illustration disc. */
@Composable
internal fun FactDisc(id: Int, size: Dp) {
    PlanetDisc(color = factColor(id), size = size)
}

/** The illustration slot on a fact: two lit bodies on the well colour. */
@Composable
internal fun FactIllustration(id: Int, modifier: Modifier = Modifier) {
    val main = factColor(id)
    val second = factAccent(id).let { if (it == main) StarDeep else it }
    Box(
        modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(Corners.Card)
            .background(Well),
    ) {
        Canvas(Modifier.fillMaxWidth().height(240.dp)) {
            fun body(c: Color, center: Offset, r: Float) {
                drawCircle(c, r, center)
                drawCircle(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent), center = center - Offset(r * 0.4f, r * 0.45f), radius = r),
                    r,
                    center,
                )
                drawCircle(
                    Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), center = center - Offset(r * 0.35f, r * 0.4f), radius = r * 1.8f),
                    r,
                    center,
                )
            }
            body(main, Offset(size.width * 0.32f, size.height * 0.6f), size.height * 0.24f)
            body(second, Offset(size.width * 0.72f, size.height * 0.36f), size.height * 0.18f)
        }
        Text(
            stringResource(R.string.space_fact_illustration),
            style = AlmanacCaption,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
        )
    }
}
