package com.app.exoplanethunter.exoplanet.domain.model

/**
 * A host star's position in space, used by the 3D galactic-neighborhood map.
 *
 * Right ascension / declination / distance come straight from the catalog and
 * are projected into Cartesian coordinates on screen. Only systems that have all
 * three values are included.
 */
data class StarPosition(
    val id: Long,
    val hostName: String,
    val ra: Double,
    val dec: Double,
    val distanceParsec: Double,
    val numPlanets: Int,
    val spectralType: String?
)
