package com.workspace.exoplanethunter.data.local.ml

import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.domain.model.HabitabilityInsight
import com.workspace.exoplanethunter.domain.model.PlanetClassification
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class HabitabilityClassifier {

    fun classify(planet: Exoplanet): HabitabilityInsight {
        val scores = mutableMapOf<String, Double>()
        val insights = mutableListOf<String>()

        // Temperature score (Earth ~288K is ideal)
        val tempScore = planet.equilibriumTempK?.let { temp ->
            val idealTemp = 288.0
            val score = gaussianScore(temp, idealTemp, 80.0)
            when {
                temp < 180 -> insights.add("Extremely cold: ${temp.toInt()}K - well below freezing point of water")
                temp in 180.0..230.0 -> insights.add("Very cold: ${temp.toInt()}K - possible with greenhouse effects")
                temp in 230.0..280.0 -> insights.add("Cool: ${temp.toInt()}K - potentially habitable with atmosphere")
                temp in 280.0..320.0 -> insights.add("Near-Earth temperature: ${temp.toInt()}K - excellent for liquid water")
                temp in 320.0..400.0 -> insights.add("Hot: ${temp.toInt()}K - possible with cloud cover")
                else -> insights.add("Extreme temperature: ${temp.toInt()}K - hostile to known life")
            }
            score
        } ?: 0.0
        scores["Temperature"] = tempScore

        // Size score (Earth radius ~1.0 is ideal, up to ~1.6 super-Earth)
        val sizeScore = planet.planetRadiusEarth?.let { radius ->
            val score = when {
                radius < 0.5 -> 0.2 // Too small to hold atmosphere
                radius in 0.5..0.8 -> 0.5 + (radius - 0.5) * 1.67
                radius in 0.8..1.3 -> 0.9 + gaussianScore(radius, 1.0, 0.3) * 0.1
                radius in 1.3..2.0 -> 0.7 - (radius - 1.3) * 0.5
                radius in 2.0..4.0 -> 0.3 - (radius - 2.0) * 0.1
                else -> 0.05
            }
            when {
                radius < 0.5 -> insights.add("Sub-Earth size (${String.format("%.1f", radius)}R⊕) - too small for atmosphere retention")
                radius in 0.5..1.5 -> insights.add("Earth-like size (${String.format("%.1f", radius)}R⊕) - suitable for rocky composition")
                radius in 1.5..2.5 -> insights.add("Super-Earth (${String.format("%.1f", radius)}R⊕) - may have thick atmosphere")
                radius in 2.5..6.0 -> insights.add("Mini-Neptune (${String.format("%.1f", radius)}R⊕) - likely gaseous")
                radius in 6.0..15.0 -> insights.add("Neptune-like (${String.format("%.1f", radius)}R⊕) - ice/gas giant")
                else -> insights.add("Jupiter-class (${String.format("%.1f", radius)}R⊕) - gas giant")
            }
            score.coerceIn(0.0, 1.0)
        } ?: 0.0
        scores["Size"] = sizeScore

        // Insolation score (Earth = 1.0 S⊕)
        val insolationScore = planet.insolationFlux?.let { flux ->
            val score = gaussianScore(flux, 1.0, 0.5)
            when {
                flux < 0.3 -> insights.add("Low stellar energy (${String.format("%.2f", flux)} S⊕) - likely too cold")
                flux in 0.3..0.8 -> insights.add("Moderate stellar energy (${String.format("%.2f", flux)} S⊕) - outer habitable zone")
                flux in 0.8..1.5 -> insights.add("Earth-like stellar energy (${String.format("%.2f", flux)} S⊕) - habitable zone")
                flux in 1.5..3.0 -> insights.add("High stellar energy (${String.format("%.2f", flux)} S⊕) - inner habitable zone")
                else -> insights.add("Extreme stellar radiation (${String.format("%.1f", flux)} S⊕) - likely too hot")
            }
            score
        } ?: 0.0
        scores["Insolation"] = insolationScore

        // Orbital stability score
        val orbitalScore = planet.eccentricity?.let { ecc ->
            val score = (1.0 - ecc).coerceIn(0.0, 1.0).pow(2)
            when {
                ecc < 0.05 -> insights.add("Nearly circular orbit (e=${String.format("%.3f", ecc)}) - stable climate")
                ecc in 0.05..0.2 -> insights.add("Mildly eccentric orbit (e=${String.format("%.2f", ecc)}) - seasonal variation")
                ecc in 0.2..0.4 -> insights.add("Eccentric orbit (e=${String.format("%.2f", ecc)}) - extreme seasons")
                else -> insights.add("Highly eccentric orbit (e=${String.format("%.2f", ecc)}) - dramatic temperature swings")
            }
            score
        } ?: 0.5
        scores["Orbital Stability"] = orbitalScore

        // Stellar type score
        val stellarScore = planet.stellarEffectiveTempK?.let { sTemp ->
            val score = when {
                sTemp < 3500 -> 0.4  // M-dwarf - tidal locking issues
                sTemp in 3500.0..5000.0 -> 0.7  // K-type - long lived, stable
                sTemp in 5000.0..6500.0 -> 1.0  // G-type (Sun-like)
                sTemp in 6500.0..7500.0 -> 0.6  // F-type - higher UV
                else -> 0.2  // A/B/O types - too hot, short lived
            }
            val spectralClass = when {
                sTemp < 3500 -> "M-dwarf"
                sTemp < 5000 -> "K-type"
                sTemp < 6000 -> "G-type (Sun-like)"
                sTemp < 7500 -> "F-type"
                else -> "Hot star"
            }
            insights.add("Host star: $spectralClass (${sTemp.toInt()}K) - ${if (score > 0.6) "favorable" else "challenging"} for habitability")
            score
        } ?: 0.0
        scores["Star Type"] = stellarScore

        // Mass score
        val massScore = planet.planetMassEarth?.let { mass ->
            val score = when {
                mass < 0.1 -> 0.1
                mass in 0.1..0.5 -> 0.3 + (mass - 0.1) * 1.5
                mass in 0.5..3.0 -> 0.8 + gaussianScore(mass, 1.0, 1.0) * 0.2
                mass in 3.0..10.0 -> 0.6 - (mass - 3.0) * 0.05
                else -> 0.1
            }
            score.coerceIn(0.0, 1.0)
        } ?: 0.0
        scores["Mass"] = massScore

        // Calculate weighted overall score
        val weights = mapOf(
            "Temperature" to 0.30,
            "Size" to 0.20,
            "Insolation" to 0.20,
            "Orbital Stability" to 0.10,
            "Star Type" to 0.10,
            "Mass" to 0.10
        )

        val overallScore = scores.entries.sumOf { (key, value) ->
            value * (weights[key] ?: 0.0)
        }.coerceIn(0.0, 1.0)

        val classification = classifyPlanet(planet)

        return HabitabilityInsight(
            overallScore = overallScore,
            scores = scores,
            insights = insights,
            classification = classification
        )
    }

    private fun classifyPlanet(planet: Exoplanet): PlanetClassification {
        val radius = planet.planetRadiusEarth
        val mass = planet.planetMassEarth
        val temp = planet.equilibriumTempK

        return when {
            radius != null && radius < 0.8 -> PlanetClassification.SUB_EARTH
            radius != null && radius <= 1.6 && (mass == null || mass <= 10) ->
                if (temp != null && temp in 200.0..350.0) PlanetClassification.POTENTIALLY_HABITABLE
                else PlanetClassification.ROCKY
            radius != null && radius <= 4.0 -> PlanetClassification.SUPER_EARTH
            radius != null && radius <= 10.0 -> PlanetClassification.NEPTUNE_LIKE
            radius != null && radius > 10.0 -> PlanetClassification.GAS_GIANT
            mass != null && mass > 300 -> PlanetClassification.GAS_GIANT
            mass != null && mass > 10 -> PlanetClassification.NEPTUNE_LIKE
            else -> PlanetClassification.UNKNOWN
        }
    }

    private fun gaussianScore(value: Double, mean: Double, sigma: Double): Double {
        return exp(-0.5 * ((value - mean) / sigma).pow(2))
    }
}
