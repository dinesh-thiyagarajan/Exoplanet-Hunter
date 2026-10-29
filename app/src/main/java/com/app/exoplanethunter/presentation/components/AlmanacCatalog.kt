package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.graphics.Color
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.TempCold
import com.app.exoplanethunter.presentation.theme.TempFrozen
import com.app.exoplanethunter.presentation.theme.TempHotColor
import com.app.exoplanethunter.presentation.theme.TempScorched
import com.app.exoplanethunter.presentation.theme.TempTemperate
import com.app.exoplanethunter.presentation.theme.TempUnknown
import kotlin.math.roundToInt

// ===========================================================================
// Catalogue helpers shared across list rows, detail headers, and the galaxy map.
// ===========================================================================

private const val PARSEC_TO_LY = 3.26156

/** A stable catalogue number, e.g. "EH-0042". */
fun catalogueId(id: Long): String = "EH-%04d".format(id)

/** Star class shorthand from spectral type, e.g. "M-type", or null when unknown. */
fun starTypeLabel(spectralType: String?): String? =
    spectralType?.trim()?.firstOrNull()?.uppercaseChar()
        ?.takeIf { it in "OBAFGKM" }
        ?.let { "$it-type" }

/** Size class for the catalogue line, e.g. "Super-Earth", "Hot Jupiter", "Size unknown". */
fun classificationLabel(planet: Exoplanet): String {
    val r = planet.planetRadiusEarth
    val hot = (planet.equilibriumTempK ?: 0.0) > 1000.0 ||
        (planet.orbitalPeriodDays ?: Double.MAX_VALUE) < 10.0
    return when {
        r == null -> "Size unknown"
        r < 0.8 -> "Sub-Earth"
        r < 1.25 -> "Earth-size"
        r < 2.0 -> "Super-Earth"
        r < 4.0 -> "Sub-Neptune"
        r < 8.0 -> "Neptune-size"
        r < 10.0 -> "Saturn-size"
        hot -> "Hot Jupiter"
        else -> "Gas giant"
    }
}

/** The six-step temperature scale. Colour carries meaning, so the word is always shown too. */
enum class TempBucket(val label: String, val color: Color) {
    Scorched("Scorched", TempScorched),
    Hot("Hot", TempHotColor),
    Temperate("Temperate", TempTemperate),
    Cold("Cold", TempCold),
    Frozen("Frozen", TempFrozen),
    Unknown("Unknown", TempUnknown);

    companion object {
        fun of(tempK: Double?): TempBucket = when {
            tempK == null -> Unknown
            tempK < 150 -> Frozen
            tempK < 200 -> Cold
            tempK <= 320 -> Temperate
            tempK <= 1000 -> Hot
            else -> Scorched
        }
    }
}

/** Temperature bucket colour — the only colour scale that carries meaning. */
fun temperatureColor(tempK: Double?): Color = TempBucket.of(tempK).color

/** Temperature bucket word, e.g. "Temperate". */
fun temperatureLabel(tempK: Double?): String = TempBucket.of(tempK).label

/** Distance rendered as light-years, e.g. "4.2 ly" or "1,206 ly"; "—" when unknown. */
fun formatLightYears(parsec: Double?): String {
    if (parsec == null) return "—"
    val ly = parsec * PARSEC_TO_LY
    return if (ly < 10) "%.1f ly".format(ly) else "%,d ly".format(ly.roundToInt())
}
