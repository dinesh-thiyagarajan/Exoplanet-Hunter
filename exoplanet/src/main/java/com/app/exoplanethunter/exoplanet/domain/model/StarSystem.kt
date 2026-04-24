package com.app.exoplanethunter.exoplanet.domain.model

data class StarSystem(
    val id: Long,
    val hostName: String,
    val numStars: Int,
    val numPlanets: Int,
    val stellarEffectiveTempK: Double?,
    val stellarRadiusSolar: Double?,
    val stellarMassSolar: Double?,
    val stellarMetallicity: Double?,
    val stellarSurfaceGravity: Double?,
    val spectralType: String?,
    val distanceParsec: Double?,
    val ra: Double?,
    val dec: Double?,
    val planets: List<Exoplanet>
)
