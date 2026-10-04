package com.app.exoplanethunter.presentation.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

// ===========================================================================
// What a planet probably looks like, inferred from its catalogue numbers.
// Nobody has imaged these worlds; this turns radius, mass, density and
// temperature into the most plausible bulk composition and surface, which
// drives both the rendered sphere and the "likely made of" readout.
// ===========================================================================

private const val EARTH_DENSITY = 5.51 // g/cm³

/** Earth radii of the reference bodies used for size comparisons. */
const val NEPTUNE_RADIUS_EARTH = 3.88
const val JUPITER_RADIUS_EARTH = 11.21

/**
 * A close-in planet whose rotation has synchronised with its orbit, so one face
 * is in permanent daylight and the other in eternal night. Heuristic: a short
 * orbital period (tight orbit) — these are widely presumed to be tidally locked.
 */
fun isLikelyTidallyLocked(planet: Exoplanet): Boolean {
    val period = planet.orbitalPeriodDays ?: return false
    return period < 10.0
}

/** Bulk density in g/cm³ from mass and radius, when both are known. */
fun Exoplanet.bulkDensity(): Double? {
    val m = planetMassEarth ?: return null
    val r = planetRadiusEarth?.takeIf { it > 0.0 } ?: return null
    return EARTH_DENSITY * m / (r * r * r)
}

/** Surface gravity in Earth g (g ∝ M / R²), when both are known. */
fun Exoplanet.surfaceGravity(): Double? {
    val m = planetMassEarth ?: return null
    val r = planetRadiusEarth?.takeIf { it > 0.0 } ?: return null
    return m / (r * r)
}

private const val AU_PER_SOLAR_RADIUS = 0.00465047

/**
 * Equilibrium temperature, or an estimate when the catalogue leaves it blank:
 * from insolation (T ≈ 278 K · S^¼), else from the star and orbit
 * (T = T★ · √(R★ / 2a), zero albedo). The orbit's size comes from the catalogue, or
 * from the period and star mass by Kepler's third law.
 */
fun Exoplanet.estimatedTempK(): Double? {
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

/** Most plausible bulk composition, from density when available, else radius, else mass. */
enum class Composition(@StringRes val labelRes: Int) {
    IronRich(R.string.composition_iron_rich),
    Rocky(R.string.composition_rocky),
    RockyOrWater(R.string.composition_rocky_or_water),
    WaterRich(R.string.composition_water_rich),
    MiniNeptune(R.string.composition_mini_neptune),
    IceGiant(R.string.composition_ice_giant),
    GasGiant(R.string.composition_gas_giant),
    PuffyGiant(R.string.composition_puffy_giant),
    HotJupiter(R.string.composition_hot_jupiter),
    Unknown(R.string.composition_unknown),
}

/** Which measurements the composition was inferred from — shown so the guess is honest. */
enum class CompositionBasis(@StringRes val labelRes: Int) {
    Density(R.string.composition_basis_density),
    Radius(R.string.composition_basis_radius),
    Mass(R.string.composition_basis_mass),
    None(R.string.composition_basis_none),
}

fun Exoplanet.composition(): Pair<Composition, CompositionBasis> {
    val r = planetRadiusEarth
    val m = planetMassEarth
    val rho = bulkDensity()
    val hot = (estimatedTempK() ?: 0.0) > 1000.0

    fun giant(): Composition = when {
        hot -> Composition.HotJupiter
        rho != null && rho < 0.5 -> Composition.PuffyGiant
        else -> Composition.GasGiant
    }

    return when {
        // Density separates rock, water and gas far better than size alone.
        rho != null && r != null -> when {
            r >= 8.0 -> giant()
            r >= 4.0 -> Composition.IceGiant
            r < 1.6 -> if (rho >= 7.0) Composition.IronRich else Composition.Rocky
            rho >= 4.5 -> Composition.Rocky
            rho >= 2.5 -> Composition.WaterRich
            else -> Composition.MiniNeptune
        } to CompositionBasis.Density

        // Radius alone: the "radius valley" near 1.5–2 R⊕ splits rocky from gas-rich worlds.
        r != null -> when {
            r < 1.5 -> Composition.Rocky
            r < 2.0 -> Composition.RockyOrWater
            r < 4.0 -> Composition.MiniNeptune
            r < 8.0 -> Composition.IceGiant
            else -> giant()
        } to CompositionBasis.Radius

        m != null -> when {
            m < 2.0 -> Composition.Rocky
            m < 10.0 -> Composition.RockyOrWater
            m < 50.0 -> Composition.IceGiant
            else -> giant()
        } to CompositionBasis.Mass

        else -> Composition.Unknown to CompositionBasis.None
    }
}

/**
 * Whether the AI model's planet type is consistent with [composition]. The model was trained
 * on coarse radius bins (it calls anything under 15 R⊕ "Neptune-like", though Jupiter is
 * 11 R⊕), so where density says otherwise we'd rather show nothing than contradict ourselves.
 */
fun PlanetClassification.agreesWith(composition: Composition): Boolean {
    if (composition == Composition.Unknown) return this != PlanetClassification.UNKNOWN
    return when (this) {
        PlanetClassification.ROCKY, PlanetClassification.SUPER_EARTH, PlanetClassification.POTENTIALLY_HABITABLE ->
            composition in setOf(Composition.IronRich, Composition.Rocky, Composition.RockyOrWater, Composition.WaterRich)
        PlanetClassification.SUB_NEPTUNE ->
            composition in setOf(Composition.RockyOrWater, Composition.WaterRich, Composition.MiniNeptune)
        PlanetClassification.NEPTUNE_LIKE ->
            composition in setOf(Composition.MiniNeptune, Composition.IceGiant)
        PlanetClassification.GAS_GIANT ->
            composition in setOf(Composition.GasGiant, Composition.PuffyGiant, Composition.HotJupiter)
        PlanetClassification.UNKNOWN -> false // says nothing the composition line doesn't
    }
}

/** The kind of surface the renderer paints. */
enum class SurfaceKind {
    Lava, Desert, Temperate, Frozen, Barren,
    Ocean, Haze, IceGiant, CoolGiant, WarmGiant, HotJupiter,
}

fun Exoplanet.surfaceKind(): SurfaceKind {
    val t = estimatedTempK()
    val rocky: SurfaceKind = when {
        t == null -> SurfaceKind.Barren
        t > 1000 -> SurfaceKind.Lava
        t > 400 -> SurfaceKind.Desert
        t > 180 -> SurfaceKind.Temperate
        else -> SurfaceKind.Frozen
    }
    return when (composition().first) {
        Composition.IronRich, Composition.Rocky, Composition.RockyOrWater -> rocky
        Composition.WaterRich -> when {
            t == null -> SurfaceKind.Ocean
            t > 500 -> SurfaceKind.Haze // steam envelope
            t > 180 -> SurfaceKind.Ocean
            else -> SurfaceKind.Frozen
        }
        Composition.MiniNeptune -> SurfaceKind.Haze
        Composition.IceGiant -> SurfaceKind.IceGiant
        Composition.HotJupiter -> SurfaceKind.HotJupiter
        Composition.GasGiant, Composition.PuffyGiant -> when {
            t != null && t > 350 -> SurfaceKind.WarmGiant
            else -> SurfaceKind.CoolGiant
        }
        Composition.Unknown -> SurfaceKind.Barren
    }
}

/** Only cold giants get rings — heat sublimates the ice that makes rings like Saturn's. */
fun SurfaceKind.hasRings(planet: Exoplanet): Boolean {
    val t = planet.estimatedTempK() ?: return false // don't guess rings without a temperature
    return this == SurfaceKind.CoolGiant && t < 200.0
}

/**
 * Colour of a star's light from its effective temperature (Tanner Helland's blackbody fit),
 * softened toward white so the planet's own colours still read.
 */
fun starLightColor(tempK: Double?): Color {
    val t = ((tempK ?: 5772.0).coerceIn(1000.0, 40000.0)) / 100.0
    val r = if (t <= 66) 255.0 else 329.698727446 * (t - 60).pow(-0.1332047592)
    val g = if (t <= 66) 99.4708025861 * ln(t) - 161.1195681661 else 288.1221695283 * (t - 60).pow(-0.0755148492)
    val b = when {
        t >= 66 -> 255.0
        t <= 19 -> 0.0
        else -> 138.5177312231 * ln(t - 10) - 305.0447927307
    }
    fun ch(v: Double) = (v.coerceIn(0.0, 255.0) / 255.0).toFloat()
    val soften = 0.35f
    return Color(
        red = ch(r) * (1 - soften) + soften,
        green = ch(g) * (1 - soften) + soften,
        blue = ch(b) * (1 - soften) + soften,
    )
}

/** Representative flat colour for a surface kind — used for the to-scale size strip. */
fun SurfaceKind.swatch(): Color = when (this) {
    SurfaceKind.Lava -> Color(0xFF7A3A22)
    SurfaceKind.Desert -> Color(0xFFC9A46A)
    SurfaceKind.Temperate -> Color(0xFF3F7FB5)
    SurfaceKind.Frozen -> Color(0xFFD5E2EE)
    SurfaceKind.Barren -> Color(0xFF9A948C)
    SurfaceKind.Ocean -> Color(0xFF2B6CB0)
    SurfaceKind.Haze -> Color(0xFF9CC3D0)
    SurfaceKind.IceGiant -> Color(0xFF5DA9D6)
    SurfaceKind.CoolGiant -> Color(0xFFD2B48C)
    SurfaceKind.WarmGiant -> Color(0xFF6E8FB8)
    SurfaceKind.HotJupiter -> Color(0xFF8C3B2A)
}
