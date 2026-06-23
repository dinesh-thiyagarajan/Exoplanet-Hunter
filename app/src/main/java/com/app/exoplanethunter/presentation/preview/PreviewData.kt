package com.app.exoplanethunter.presentation.preview

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem

/**
 * Sample domain objects for `@Preview` composables only. Not used at runtime.
 */
internal object PreviewData {

    val planet = Exoplanet(
        id = 1,
        planetName = "Kepler-452 b",
        hostName = "Kepler-452",
        numStars = 1,
        numPlanets = 1,
        discoveryMethod = "Transit",
        discoveryYear = 2015,
        discoveryFacility = "Kepler",
        orbitalPeriodDays = 384.84,
        orbitSemiMajorAxisAu = 1.046,
        planetRadiusEarth = 1.63,
        planetRadiusJupiter = 0.145,
        planetMassEarth = 5.0,
        planetMassJupiter = 0.0157,
        eccentricity = 0.0,
        insolationFlux = 1.1,
        equilibriumTempK = 265.0,
        stellarEffectiveTempK = 5757.0,
        stellarRadiusSolar = 1.11,
        stellarMassSolar = 1.04,
        stellarMetallicity = 0.21,
        stellarSurfaceGravity = 4.32,
        spectralType = "G2V",
        distanceParsec = 551.0,
        ra = 285.679,
        dec = 44.279
    )

    val hotPlanet = planet.copy(
        id = 2,
        planetName = "55 Cancri e",
        hostName = "55 Cancri",
        planetRadiusEarth = 1.88,
        planetMassEarth = 8.08,
        equilibriumTempK = 1958.0,
        distanceParsec = 12.6
    )

    val planets = listOf(planet, hotPlanet)

    val insight = HabitabilityInsight(
        overallScore = 0.72,
        scores = linkedMapOf(
            "Habitability" to 0.72,
            "Temperature Zone" to 0.81,
            "Size Compatibility" to 0.64,
            "Atmospheric Potential" to 0.55,
            "Stellar Stability" to 0.9
        ),
        insights = listOf(
            "Sits within the conservative habitable zone of a Sun-like star.",
            "Radius suggests a possibly rocky composition."
        ),
        classification = PlanetClassification.POTENTIALLY_HABITABLE,
        habitabilityReliable = true
    )

    val starSystem = StarSystem(
        id = 1,
        hostName = "TRAPPIST-1",
        numStars = 1,
        numPlanets = 7,
        stellarEffectiveTempK = 2566.0,
        stellarRadiusSolar = 0.12,
        stellarMassSolar = 0.089,
        stellarMetallicity = 0.04,
        stellarSurfaceGravity = 5.24,
        spectralType = "M8V",
        distanceParsec = 12.47,
        ra = 346.622,
        dec = -5.041,
        planets = planets
    )
}
