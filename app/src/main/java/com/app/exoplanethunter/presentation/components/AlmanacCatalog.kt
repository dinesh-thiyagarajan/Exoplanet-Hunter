package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.graphics.Color
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.TempCold
import com.app.exoplanethunter.presentation.theme.TempHot
import com.app.exoplanethunter.presentation.theme.TempTemperate
import com.app.exoplanethunter.presentation.theme.TempUnknown
import kotlin.math.roundToInt

// ===========================================================================
// Observatory Almanac catalogue helpers — the logbook vocabulary shared across
// list rows, detail headers, and the galaxy map.
// ===========================================================================

private const val PARSEC_TO_LY = 3.26156

/** A stable, logbook-style catalogue number, e.g. "EH-0042". */
fun catalogueId(id: Long): String = "EH-%04d".format(id)

/** Star class shorthand from spectral type, e.g. "M-DWARF", "G-TYPE". */
fun starTypeLabel(spectralType: String?): String =
    when (spectralType?.trim()?.firstOrNull()?.uppercaseChar()) {
        'O' -> "O-TYPE"
        'B' -> "B-TYPE"
        'A' -> "A-TYPE"
        'F' -> "F-TYPE"
        'G' -> "G-TYPE"
        'K' -> "K-DWARF"
        'M' -> "M-DWARF"
        else -> "UNCLASSED"
    }

/** Uppercase classification for the catalogue, e.g. "ROCKY", "HOT JUPITER". */
fun classificationLabel(planet: Exoplanet): String {
    val r = planet.planetRadiusEarth
    val base = when {
        r == null -> "UNSIZED"
        r < 1.25 -> "ROCKY"
        r < 2.0 -> "SUPER-EARTH"
        r < 6.0 -> "NEPTUNE-LIKE"
        else -> "JUPITER-LIKE"
    }
    val hot = (planet.equilibriumTempK ?: 0.0) > 1000.0 ||
        (planet.orbitalPeriodDays ?: Double.MAX_VALUE) < 10.0
    return if (base == "JUPITER-LIKE" && hot) "HOT JUPITER" else base
}

/** Temperature bucket colour — the only semantic encoding in the design. */
fun temperatureColor(tempK: Double?): Color = when {
    tempK == null -> TempUnknown
    tempK < 200 -> TempCold
    tempK <= 320 -> TempTemperate
    else -> TempHot
}

/** Temperature bucket label, e.g. "TEMPERATE", "FROZEN", "SCORCHED". */
fun temperatureLabel(tempK: Double?): String = when {
    tempK == null -> "UNKNOWN"
    tempK < 200 -> "FROZEN"
    tempK <= 320 -> "TEMPERATE"
    else -> "SCORCHED"
}

/** Distance rendered as light-years, e.g. "4.24 ly" or "1,206 ly". */
fun formatLightYears(parsec: Double?): String {
    if (parsec == null) return "— ly"
    val ly = parsec * PARSEC_TO_LY
    return if (ly < 100) "%.2f ly".format(ly) else "%,d ly".format(ly.roundToInt())
}
