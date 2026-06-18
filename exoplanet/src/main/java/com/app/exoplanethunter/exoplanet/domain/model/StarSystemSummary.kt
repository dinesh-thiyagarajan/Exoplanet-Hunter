package com.app.exoplanethunter.exoplanet.domain.model

data class StarSystemSummary(
    val id: Long,
    val hostName: String,
    val numPlanets: Int = 0,
    val numStars: Int = 0,
    val distanceParsec: Double? = null,
    val spectralType: String? = null
)
