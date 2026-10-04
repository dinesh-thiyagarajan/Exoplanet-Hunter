package com.app.exoplanethunter.exoplanet.domain.model

import kotlin.math.pow
import kotlin.math.sqrt

private const val AU_PER_SOLAR_RADIUS = 0.00465047

/**
 * Equilibrium temperature, or an estimate when the catalogue leaves it blank:
 * from insolation (T ≈ 278 K · S^¼), else from the star and orbit
 * (T = T★ · √(R★ / 2a), zero albedo). The orbit's size comes from the catalogue, or
 * from the period and star mass by Kepler's third law. Null when none of that is known.
 */
fun Exoplanet.estimatedEquilibriumTempK(): Double? {
    equilibriumTempK?.let { return it }
    insolationFlux?.takeIf { it > 0.0 }?.let { return 278.0 * it.pow(0.25) }
    val starTemp = stellarEffectiveTempK ?: return null
    val starRadiusAu = (stellarRadiusSolar ?: return null) * AU_PER_SOLAR_RADIUS
    val a = orbitSemiMajorAxisAu
        ?: orbitalPeriodDays?.let { p -> stellarMassSolar?.let { m -> (m * (p / 365.25).pow(2)).pow(1.0 / 3.0) } }
        ?: return null
    if (a <= 0.0) return null
    return starTemp * sqrt(starRadiusAu / (2 * a))
}
