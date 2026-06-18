package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.graphics.Color
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CoolBlue
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.FrozenBlue
import com.app.exoplanethunter.presentation.theme.HotOrange
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.ScorchingRed
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.TemperateGreen
import com.app.exoplanethunter.presentation.theme.WarmYellow

/**
 * Cheap, ML-free planet categorisation by radius (falling back to mass) — mirrors the
 * radius bins used to train the planet-type model, but computed instantly for list rows.
 */
fun planetTypeLabel(planet: Exoplanet): String {
    val r = planet.planetRadiusEarth
    val m = planet.planetMassEarth
    return when {
        r != null -> when {
            r < 1.25 -> "Rocky"
            r < 2.0 -> "Super-Earth"
            r < 6.0 -> "Sub-Neptune"
            r < 15.0 -> "Neptune-like"
            else -> "Gas Giant"
        }
        m != null -> when {
            m < 2.0 -> "Rocky"
            m < 10.0 -> "Super-Earth"
            m < 50.0 -> "Neptune-like"
            else -> "Gas Giant"
        }
        else -> "Unknown"
    }
}

fun planetTypeColor(typeLabel: String): Color = when (typeLabel) {
    "Rocky" -> TemperateGreen
    "Super-Earth" -> AuroraGreen
    "Sub-Neptune" -> CosmicCyan
    "Neptune-like" -> FrozenBlue
    "Gas Giant" -> SolarOrange
    else -> NebulaPink
}

/**
 * The colour used to render a planet sphere. Driven by equilibrium temperature when known
 * (physically meaningful glow), otherwise by planet type so it's never a dull grey.
 */
fun planetAccentColor(planet: Exoplanet): Color {
    val t = planet.equilibriumTempK
    return when {
        t != null && t < 200 -> FrozenBlue
        t != null && t < 300 -> CoolBlue
        t != null && t < 400 -> TemperateGreen
        t != null && t < 600 -> WarmYellow
        t != null && t < 1000 -> HotOrange
        t != null -> ScorchingRed
        else -> planetTypeColor(planetTypeLabel(planet))
    }
}
