package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.ui.graphics.Color
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.components.TempBucket
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.hasRings
import com.app.exoplanethunter.presentation.components.surfaceKind
import com.app.exoplanethunter.presentation.components.swatch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ===========================================================================
// Orbit mechanics behind the orrery — kept free of Compose so it can be unit-tested.
// ===========================================================================

/** A planet with everything the orrery needs, worked out once. */
internal class OrbitBody(
    val planet: Exoplanet,
    val label: String,
    val aAu: Double,
    val aEstimated: Boolean,
    val e: Double,
    val omega: Double, // orientation of the ellipse
    val m0: Double, // where it starts on its orbit
    val periodDays: Double,
    val radiusEarth: Double?,
    val color: Color,
    val rings: Boolean,
)

internal fun buildBodies(system: StarSystem): List<OrbitBody> {
    val starMass = system.stellarMassSolar?.takeIf { it > 0.0 } ?: 1.0
    return system.planets.mapNotNull { p ->
        // Fill in whichever of orbit size / period is missing via Kepler's third law:
        // P² (years) = a³ (AU) / M★ (solar masses).
        val a = p.orbitSemiMajorAxisAu?.takeIf { it > 0.0 }
            ?: p.orbitalPeriodDays?.takeIf { it > 0.0 }?.let { (starMass * (it / 365.25).pow(2)).pow(1.0 / 3.0) }
            ?: return@mapNotNull null
        val period = p.orbitalPeriodDays?.takeIf { it > 0.0 }
            ?: (sqrt(a * a * a / starMass) * 365.25)
        val rnd = Random(p.id)
        OrbitBody(
            planet = p,
            label = p.planetName.removePrefix(system.hostName).trim().ifEmpty { p.planetName },
            aAu = a,
            aEstimated = p.orbitSemiMajorAxisAu == null,
            e = (p.eccentricity ?: 0.0).coerceIn(0.0, 0.95),
            omega = rnd.nextDouble(0.0, 2 * PI),
            m0 = rnd.nextDouble(0.0, 2 * PI),
            periodDays = period,
            radiusEarth = p.planetRadiusEarth?.takeIf { it > 0.0 },
            color = TempBucket.of(p.estimatedTempK()).color,
            rings = p.surfaceKind().hasRings(p),
        )
    }.sortedBy { it.aAu }
}

/**
 * Conservative habitable zone (AU): where a rocky planet could keep liquid surface water,
 * per Kopparapu et al. (2014) — runaway greenhouse (inner edge) to maximum greenhouse
 * (outer edge). The flux limits depend on the star's temperature, because redder light warms
 * planets more efficiently; a Sun-calibrated rule would put a red dwarf's zone too close in.
 * Distance = √(L / S_eff), with L = R² (T / 5772 K)⁴.
 */
internal fun habitableZone(system: StarSystem): ClosedFloatingPointRange<Double>? {
    val r = system.stellarRadiusSolar?.takeIf { it > 0.0 } ?: return null
    val t = system.stellarEffectiveTempK?.takeIf { it > 0.0 } ?: return null
    val luminosity = r * r * (t / 5772.0).pow(4)
    // The fits are valid for 2,600–7,200 K; clamp outside that range.
    val dt = t.coerceIn(2600.0, 7200.0) - 5780.0
    fun seff(sun: Double, a: Double, b: Double, c: Double, d: Double) =
        sun + a * dt + b * dt * dt + c * dt * dt * dt + d * dt * dt * dt * dt
    val inner = seff(1.0385, 1.2456e-4, 1.4612e-8, -7.6345e-12, -1.7511e-15)
    val outer = seff(0.3507, 5.9578e-5, 1.6707e-9, -3.0058e-12, -5.1925e-16)
    return sqrt(luminosity / inner)..sqrt(luminosity / outer)
}

/** Position in the orbital plane (AU, star at origin) at [days], via Kepler's equation. */
internal fun OrbitBody.positionAt(days: Double): Pair<Double, Double> {
    val mean = m0 + 2 * PI * days / periodDays
    var ecc = mean
    repeat(6) { ecc -= (ecc - e * sin(ecc) - mean) / (1 - e * cos(ecc)) }
    return orbitPoint(ecc)
}

/** Point on the orbit for eccentric anomaly [ecc], rotated by the ellipse's orientation. */
internal fun OrbitBody.orbitPoint(ecc: Double): Pair<Double, Double> {
    val x = aAu * (cos(ecc) - e)
    val y = aAu * sqrt(1 - e * e) * sin(ecc)
    return (x * cos(omega) - y * sin(omega)) to (x * sin(omega) + y * cos(omega))
}

// ---------------------------------------------------------------------------
// Multiple stars
// ---------------------------------------------------------------------------

/**
 * What's known about a system's other stars, for the handful of well-studied systems where
 * the NASA catalogue (which only counts stars) doesn't say. Hand-curated from the
 * literature; separations are approximate (often projected on the sky).
 *
 * @property companions the other stars' letters and rough distance from the planets' star, AU.
 * @property binaryPeriodDays for circumbinary systems, how long the central pair takes to
 *   orbit each other.
 */
internal data class KnownStars(
    val companions: List<Pair<String, Int>> = emptyList(),
    val binaryPeriodDays: Double? = null,
)

internal val KNOWN_STARS: Map<String, KnownStars> = mapOf(
    // Planets orbiting one star; companions far away.
    "HD 189733" to KnownStars(listOf("B" to 220)),
    "16 Cyg B" to KnownStars(listOf("A" to 850, "C" to 850)),
    "gam Cep" to KnownStars(listOf("B" to 20)),
    // Proxima is α Centauri C; its name alone would suggest companions "B" and "C".
    "Proxima Cen" to KnownStars(listOf("A" to 13_000, "B" to 13_000)),
    "55 Cnc" to KnownStars(listOf("B" to 1_065)),
    "55 Cnc B" to KnownStars(listOf("A" to 1_065)),
    "GJ 667 C" to KnownStars(listOf("A" to 230, "B" to 230)),
    "HD 41004 A" to KnownStars(listOf("B" to 23)),
    "HD 41004 B" to KnownStars(listOf("A" to 23)),
    "Kepler-444" to KnownStars(listOf("B" to 66, "C" to 66)),
    "HD 80606" to KnownStars(listOf("B" to 1_200)),
    // Circumbinary ("Tatooine") systems: planets orbit a close pair.
    "Kepler-16" to KnownStars(binaryPeriodDays = 41.08),
    "Kepler-34" to KnownStars(binaryPeriodDays = 27.80),
    "Kepler-35" to KnownStars(binaryPeriodDays = 20.73),
    "Kepler-38" to KnownStars(binaryPeriodDays = 18.80),
    "Kepler-47" to KnownStars(binaryPeriodDays = 7.45),
    "Kepler-413" to KnownStars(binaryPeriodDays = 10.12),
    "Kepler-453" to KnownStars(binaryPeriodDays = 27.32),
    "Kepler-1647" to KnownStars(binaryPeriodDays = 11.26),
    "Kepler-1661" to KnownStars(binaryPeriodDays = 28.16),
    "TOI-1338" to KnownStars(binaryPeriodDays = 14.61),
)

/**
 * Well-known circumbinary hosts whose catalogue names don't say so ("Kepler-16 b", not
 * "Kepler-16 AB b"). Everything else is detected from an "AB" in the planet name.
 */
private val KNOWN_CIRCUMBINARY_HOSTS =
    KNOWN_STARS.filterValues { it.binaryPeriodDays != null }.keys + "Kepler-64"

/**
 * True when the planets orbit *both* stars of a close pair (a "Tatooine" system), so the
 * pair belongs in the middle of the orrery. In most multi-star systems the planets orbit
 * one star and the others are far away.
 */
internal fun isCircumbinary(system: StarSystem): Boolean {
    if (system.numStars < 2) return false
    if (system.hostName in KNOWN_CIRCUMBINARY_HOSTS) return true
    return system.planets.any { p ->
        p.planetName.contains(" AB ") || p.planetName.contains("(AB)") || p.planetName.endsWith(" AB")
    }
}

/**
 * Letters for the system's other stars. The host's own letter comes from its name
 * ("16 Cyg B" is star B, so its companion is A); unnamed hosts are A. A circumbinary
 * pair is A and B together, so further companions start at C.
 */
internal fun companionLabels(system: StarSystem): List<String> {
    if (system.numStars < 2) return emptyList()
    val letters = "ABCDEF".map { it.toString() }
    val last = system.hostName.trim().substringAfterLast(' ')
    val inCentre = when {
        isCircumbinary(system) -> setOf("A", "B")
        last.length == 1 && last in letters -> setOf(last)
        else -> setOf("A")
    }
    val others = system.numStars - inCentre.size
    return letters.filter { it !in inCentre }.take(others.coerceAtLeast(0))
}

/**
 * Screen directions (radians) for companion-star pointers. Real directions aren't
 * catalogued, so each system gets its own stable choice, kept to the left and right
 * sides where there's room, and spread apart when there are several.
 */
internal fun companionAngles(hostName: String, count: Int): List<Float> {
    if (count <= 0) return emptyList()
    val rnd = Random(hostName.hashCode())
    // Upper right, lower left, lower right, upper left — then a per-system wobble.
    val slots = listOf(-0.55, PI - 0.55, 0.55, PI + 0.55)
    val start = rnd.nextInt(slots.size)
    return List(count) { i -> (slots[(start + i) % slots.size] + rnd.nextDouble(-0.25, 0.25)).toFloat() }
}

/** A distant companion star: its letter, and its rough distance in AU when known. */
internal data class Companion(val letter: String, val distanceAu: Int? = null)

/** The system's distant companions — curated where known, otherwise letters only. */
internal fun companionsOf(system: StarSystem): List<Companion> {
    KNOWN_STARS[system.hostName]?.companions?.takeIf { it.isNotEmpty() }?.let { known ->
        return known.map { (letter, au) -> Companion(letter, au) }
    }
    return companionLabels(system).map { Companion(it) }
}

/** How long a circumbinary pair takes to orbit each other: known value, else an estimate. */
internal fun pairPeriodDays(system: StarSystem, innermostPlanetPeriodDays: Double?): Double =
    KNOWN_STARS[system.hostName]?.binaryPeriodDays
        // A pair circles faster than any planet that can stably orbit both stars.
        ?: (innermostPlanetPeriodDays ?: 30.0) / 6.0
